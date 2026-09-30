package com.hungjava.bookstore.mapper;

import com.hungjava.bookstore.dto.request.OrderRequest;
import com.hungjava.bookstore.dto.response.OrderListResponse;
import com.hungjava.bookstore.dto.response.OrderResponse;
import com.hungjava.bookstore.entity.Order;
import com.hungjava.bookstore.repository.OrderProjection;
import org.springframework.stereotype.Component;

import com.hungjava.bookstore.dto.response.OrderDetailResponse;
import com.hungjava.bookstore.entity.Image;

import java.math.BigDecimal;
import java.util.List;

@Component
public class OrderMapper {

    public OrderResponse toOrderResponse(Order order) {
        if (order == null) {
            return null;
        }

        List<OrderDetailResponse> detailResponses = null;
        if (order.getListOrderDetails() != null) {
            detailResponses = order.getListOrderDetails().stream().map(d -> {
                String img = null;
                if (d.getBook() != null && d.getBook().getListImages() != null && !d.getBook().getListImages().isEmpty()) {
                    img = d.getBook().getListImages().stream()
                            .filter(Image::isIcon)
                            .map(Image::getUrlImage)
                            .findFirst()
                            .orElse(d.getBook().getListImages().get(0).getUrlImage());
                }
                BigDecimal subTotal = d.getPrice() != null
                        ? d.getPrice().multiply(BigDecimal.valueOf(d.getQuantity()))
                        : BigDecimal.ZERO;

                return OrderDetailResponse.builder()
                        .id(d.getId())
                        .bookId(d.getBook() != null ? d.getBook().getId() : 0)
                        .bookName(d.getBook() != null ? d.getBook().getName() : null)
                        .bookImage(img)
                        .price(d.getPrice())
                        .quantity(d.getQuantity())
                        .isReview(d.isReview())
                        .subTotal(subTotal)
                        .build();
            }).toList();
        }

        return OrderResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .fullName(order.getFullName())
                .phone(order.getPhone())
                .deliveryAddress(order.getDeliveryAddress())
                .note(order.getNote())
                .paymentName(order.getPayment() != null ? order.getPayment().getName() : null)
                .deliveryName(order.getDelivery() != null ? order.getDelivery().getName() : null)
                .userName(order.getUser() != null ? order.getUser().getFullName() : null)
                .totalPriceProduct(order.getTotalPriceProduct())
                .feeDelivery(order.getFeeDelivery())
                .feePayment(order.getFeePayment())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .orderDetails(detailResponses)
                .build();
    }

    public OrderListResponse toOrderListResponse(OrderProjection projection) {
        if (projection == null) {
            return null;
        }
        return OrderListResponse.builder()
                .id(projection.getId())
                .orderCode(projection.getOrderCode())
                .status(projection.getStatus())
                .totalPrice(projection.getTotalPrice())
                .deliveryName(projection.getDeliveryName())
                .paymentName(projection.getPaymentName())
                .totalItems(projection.getTotalItems() != null ? projection.getTotalItems() : 0)
                .createdAt(projection.getCreatedAt())
                .firstBookName(projection.getFirstBookName())
                .firstBookImage(projection.getFirstBookImage())
                .build();
    }

    public Order toOrder(OrderRequest request) {
        if (request == null) {
            return null;
        }
        return Order.builder()
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .deliveryAddress(request.getDeliveryAddress())
                .note(request.getNote())
                .build();
    }
}

