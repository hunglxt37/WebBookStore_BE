package com.hungjava.bookstore.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderResponse {
    int id;
    String orderCode;
    String fullName;
    String phone;
    String deliveryAddress;
    String note;
    String paymentName;
    String deliveryName;
    String userName;
    BigDecimal totalPriceProduct;
    BigDecimal feeDelivery;
    BigDecimal feePayment;
    BigDecimal totalPrice;
    String status;
    Instant createdAt;
    Instant updatedAt;
    List<OrderDetailResponse> orderDetails;
}
