package com.hungjava.bookstore.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FavoriteBookResponse {
    int id; 
    int bookId; 
    String name; 
    String author; 
    BigDecimal listPrice; 
    BigDecimal sellPrice; 
    int discountPercent; 
    int quantity;
    double avgRating;
    String thumbnailUrl; 
    String status; 
    Instant addedAt;
}
