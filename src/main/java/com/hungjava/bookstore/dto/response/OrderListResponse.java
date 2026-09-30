package com.hungjava.bookstore.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderListResponse {
    int id;
    String orderCode;
    String status;
    BigDecimal totalPrice;
    String deliveryName;
    String paymentName;
    int totalItems;
    Instant createdAt;
    String firstBookName;
    String firstBookImage;
}

