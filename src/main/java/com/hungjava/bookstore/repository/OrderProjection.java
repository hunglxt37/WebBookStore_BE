package com.hungjava.bookstore.repository;

import java.math.BigDecimal;
import java.time.Instant;

public interface OrderProjection {
    Integer getId();
    String getOrderCode();
    String getStatus();
    BigDecimal getTotalPrice();
    String getDeliveryName();
    String getPaymentName();
    Integer getTotalItems();
    Instant getCreatedAt();
    String getFirstBookName();
    String getFirstBookImage();
}
