package com.hungjava.bookstore.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderDetailResponse {
    int id;
    int bookId;
    String bookName;
    String bookImage;
    BigDecimal price;
    int quantity;
    boolean isReview;
    BigDecimal subTotal;
}
