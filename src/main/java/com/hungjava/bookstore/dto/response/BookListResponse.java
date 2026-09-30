package com.hungjava.bookstore.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookListResponse {
    Integer id;
    String name;
    String author;
    BigDecimal listPrice;
    BigDecimal sellPrice;
    int discountPercent;
    int quantity;
    String status;
    double avgRating;
    int soldQuantity;
    String thumbnailUrl;
    List<String> genres;
    Instant createdAt;
    Instant updatedAt;
}

