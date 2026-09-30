package com.hungjava.bookstore.service.impl;

import com.hungjava.bookstore.dto.request.CartItemRequest;
import com.hungjava.bookstore.dto.response.CartItemResponse;
import com.hungjava.bookstore.entity.Book;
import com.hungjava.bookstore.entity.CartItem;
import com.hungjava.bookstore.entity.Image;
import com.hungjava.bookstore.entity.User;
import com.hungjava.bookstore.exception.ApiException;
import com.hungjava.bookstore.exception.ErrorCode;
import com.hungjava.bookstore.repository.BookRepository;
import com.hungjava.bookstore.repository.CartItemRepository;
import com.hungjava.bookstore.repository.UserRepository;
import com.hungjava.bookstore.service.CartItemService;
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
public class CartItemServiceImpl implements CartItemService {

    CartItemRepository cartItemRepository;
    UserRepository userRepository;
    BookRepository bookRepository;

    @Override
    @Transactional
    public CartItemResponse addToCart(Integer userId, CartItemRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND));

        if (book.getQuantity() < request.getQuantity()) {
            throw new ApiException(ErrorCode.OUT_OF_STOCK);
        }

        CartItem cartItem = cartItemRepository.findByUserIdAndBookId(user.getId(), book.getId())
                .orElse(null);

        if (cartItem != null) {
            int newQuantity = cartItem.getQuantity() + request.getQuantity();
            if (book.getQuantity() < newQuantity) {
                throw new ApiException(ErrorCode.OUT_OF_STOCK);
            }
            cartItem.setQuantity(newQuantity);
        } else {
            cartItem = CartItem.builder()
                    .user(user)
                    .book(book)
                    .quantity(request.getQuantity())
                    .build();
        }

        CartItem savedCartItem = cartItemRepository.save(cartItem);
        return mapToResponse(savedCartItem);
    }

    @Override
    public List<CartItemResponse> getCart(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        List<CartItem> cartItems = cartItemRepository.findByUserId(user.getId());
        return cartItems.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public CartItemResponse updateQuantity(Integer userId, Integer cartItemId, Integer quantity) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ApiException(ErrorCode.CART_ITEM_NOT_FOUND));

        if (cartItem.getUser().getId() != userId) {
            throw new ApiException(ErrorCode.CART_ITEM_NOT_FOUND);
        }

        Book book = cartItem.getBook();
        if (book.getQuantity() < quantity) {
            throw new ApiException(ErrorCode.OUT_OF_STOCK);
        }

        cartItem.setQuantity(quantity);
        CartItem savedCartItem = cartItemRepository.save(cartItem);
        return mapToResponse(savedCartItem);
    }

    @Override
    @Transactional
    public void deleteCartItem(Integer userId, Integer cartItemId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ApiException(ErrorCode.CART_ITEM_NOT_FOUND));

        if (cartItem.getUser().getId() != userId) {
            throw new ApiException(ErrorCode.CART_ITEM_NOT_FOUND);
        }

        cartItemRepository.delete(cartItem);
    }

    @Override
    @Transactional
    public void clearCart(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        cartItemRepository.deleteByUserId(userId);
    }

    private CartItemResponse mapToResponse(CartItem cartItem) {
        String imageUrl = null;
        Book book = cartItem.getBook();
        if (book.getListImages() != null && !book.getListImages().isEmpty()) {
            imageUrl = book.getListImages().stream()
                    .filter(Image::isIcon)
                    .map(Image::getUrlImage)
                    .findFirst()
                    .orElse(book.getListImages().get(0).getUrlImage());
        }

        BigDecimal sellPrice = book.getSellPrice();
        BigDecimal subTotal = sellPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));

        return CartItemResponse.builder()
                .id(cartItem.getId())
                .bookId(book.getId())
                .bookName(book.getName())
                .bookImage(imageUrl)
                .listPrice(book.getListPrice())
                .sellPrice(sellPrice)
                .quantity(cartItem.getQuantity())
                .subTotal(subTotal)
                .stockQuantity(book.getQuantity())
                .status(book.getStatus())
                .build();
    }
}
