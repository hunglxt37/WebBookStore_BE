package com.hungjava.bookstore.service.impl;

import com.hungjava.bookstore.dto.request.CancelOrderRequest;
import com.hungjava.bookstore.dto.request.OrderRequest;
import com.hungjava.bookstore.dto.request.UpdateOrderStatusRequest;
import com.hungjava.bookstore.dto.response.OrderListResponse;
import com.hungjava.bookstore.dto.response.OrderResponse;
import com.hungjava.bookstore.entity.Book;
import com.hungjava.bookstore.entity.CartItem;
import com.hungjava.bookstore.entity.Delivery;
import com.hungjava.bookstore.entity.Order;
import com.hungjava.bookstore.entity.OrderDetail;
import com.hungjava.bookstore.entity.Payment;
import com.hungjava.bookstore.entity.User;
import com.hungjava.bookstore.exception.ApiException;
import com.hungjava.bookstore.exception.ErrorCode;
import com.hungjava.bookstore.mapper.OrderMapper;
import com.hungjava.bookstore.repository.BookRepository;
import com.hungjava.bookstore.repository.CartItemRepository;
import com.hungjava.bookstore.repository.DeliveryRepository;
import com.hungjava.bookstore.repository.OrderDetailRepository;
import com.hungjava.bookstore.repository.OrderProjection;
import com.hungjava.bookstore.repository.OrderRepository;
import com.hungjava.bookstore.repository.PaymentRepository;
import com.hungjava.bookstore.repository.UserRepository;
import com.hungjava.bookstore.service.OrderService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderServiceImpl implements OrderService {
    OrderRepository orderRepository;
    UserRepository userRepository;
    CartItemRepository cartItemRepository;
    BookRepository bookRepository;
    DeliveryRepository deliveryRepository;
    PaymentRepository paymentRepository;
    OrderDetailRepository orderDetailRepository;
    OrderMapper orderMapper;

    // user đặt hàng
    @Transactional
    public OrderResponse createOrderCOD(Integer userId, OrderRequest request) {

        List<CartItem> cartItems = cartItemRepository.findAllByIdInAndUserId(request.getCartItemIds(), userId);
        if (cartItems.isEmpty()) {
            throw new ApiException(ErrorCode.CART_EMPTY);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        Delivery delivery = deliveryRepository.findById(request.getDeliveryId())
                .orElseThrow(() -> new ApiException(ErrorCode.DELIVERY_NOT_FOUND));

        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new ApiException(ErrorCode.PAYMENT_NOT_FOUND));

        if (payment.getId() != 1) {
            throw new ApiException(ErrorCode.INVALID_PAYMENT_METHOD);
        }

        BigDecimal totalPriceProduct = BigDecimal.ZERO;
        for (CartItem item : cartItems) {
            Book book = item.getBook();
            if (book == null) {
                throw new ApiException(ErrorCode.BOOK_NOT_FOUND);
            }
            if (book.getQuantity() < item.getQuantity()) {
                throw new ApiException(ErrorCode.OUT_OF_STOCK);
            }
            // Trừ số lượng tồn kho và tăng số lượng đã bán
            book.setQuantity(book.getQuantity() - item.getQuantity());
            book.setSoldQuantity(book.getSoldQuantity() + item.getQuantity());
            bookRepository.save(book);

            BigDecimal sellPrice = book.getSellPrice();
            // itemTotal= sellPrice * quantity
            BigDecimal itemTotal = sellPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            totalPriceProduct = totalPriceProduct.add(itemTotal);
        }

        // Tính tổng tiền
        BigDecimal feeDelivery = delivery.getFeeDelivery() != null ? delivery.getFeeDelivery() : BigDecimal.ZERO;
        BigDecimal feePayment = payment.getFeePayment() != null ? payment.getFeePayment() : BigDecimal.ZERO;
        BigDecimal totalPrice = totalPriceProduct.add(feeDelivery).add(feePayment);

        Order order = orderMapper.toOrder(request);
        order.setUser(user);
        order.setDelivery(delivery);
        order.setPayment(payment);
        order.setTotalPriceProduct(totalPriceProduct);
        order.setFeeDelivery(feeDelivery);
        order.setFeePayment(feePayment);
        order.setTotalPrice(totalPrice);
        order.setStatus("PENDING");
        final Order savedOrder = orderRepository.save(order);

        List<OrderDetail> listOrderDetails = cartItems.stream()
                .map(item -> OrderDetail.builder()
                        .quantity(item.getQuantity())
                        .price(item.getBook().getSellPrice())
                        .isReview(false)
                        .book(item.getBook())
                        .order(savedOrder)
                        .build())
                .toList();

        orderDetailRepository.saveAll(listOrderDetails);
        savedOrder.setListOrderDetails(listOrderDetails);
        cartItemRepository.deleteAllById(request.getCartItemIds());

        return orderMapper.toOrderResponse(savedOrder);
    }

    @Transactional
    public void cancelOrderCOD(CancelOrderRequest request, Integer userId) {

        Order order = orderRepository.findByIdAndUserId(request.getId(), userId)
                .orElseThrow(() -> new ApiException(ErrorCode.ORDER_NOT_FOUND));

        if (!"PENDING".equals(order.getStatus())) {
            throw new ApiException(ErrorCode.CANNOT_CANCEL_ORDER);
        }

        if (order.getPayment() == null || order.getPayment().getId() != 1) {
            throw new ApiException(ErrorCode.INVALID_PAYMENT_METHOD);
        }

        order.setStatus("CANCELED");
        order.setNote(request.getReason());
        rollbackInventory(order);

        orderRepository.save(order);
    }

    @Override
    public OrderResponse updateStatusOrderCOD(UpdateOrderStatusRequest request) {

        Order order = orderRepository.findById(request.getId())
                .orElseThrow(() -> new ApiException(ErrorCode.ORDER_NOT_FOUND));

        String newStatus = request.getStatus();

        if ("CANCELED".equals(newStatus)) {
            if (!"CANCELED".equals(order.getStatus())) {
                rollbackInventory(order);
            }
            order.setStatus("CANCELED");
        } else {
            order.setStatus(newStatus);
        }

        Order savedOrder = orderRepository.save(order);
        return orderMapper.toOrderResponse(savedOrder);
    }

    private void rollbackInventory(Order order) {
        List<OrderDetail> orderDetails = orderDetailRepository.findOrderDetailsByOrder(order);
        for (OrderDetail detail : orderDetails) {
            Book book = detail.getBook();
            if (book != null) {
                book.setQuantity(book.getQuantity() + detail.getQuantity());
                book.setSoldQuantity(book.getSoldQuantity() - detail.getQuantity());
                bookRepository.save(book);
            }
        }
    }

    @Override
    public List<OrderListResponse> getMyOrders(Integer userId) {
        List<OrderProjection> projections = orderRepository.findAllOrdersByUserId(userId);
        return projections.stream()
                .map(orderMapper::toOrderListResponse)
                .toList();
    }

    @Override
    public List<OrderListResponse> getAllOrdersForAdmin() {
        List<OrderProjection> projections = orderRepository.findAllOrdersForAdmin();
        return projections.stream()
                .map(orderMapper::toOrderListResponse)
                .toList();
    }

    @Override
    public OrderResponse getOrderById(Integer orderId, Integer userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException(ErrorCode.ORDER_NOT_FOUND));

        if (userId != null && order.getUser().getId() != userId) {
            throw new ApiException(ErrorCode.ORDER_NOT_FOUND);
        }

        return orderMapper.toOrderResponse(order);
    }
}

