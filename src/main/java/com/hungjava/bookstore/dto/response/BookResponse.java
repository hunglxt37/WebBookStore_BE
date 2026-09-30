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
public class BookResponse {
    Integer id;
    String name;
    String author;
    String description;
    BigDecimal listPrice;
    BigDecimal sellPrice;
    int quantity;
    double avgRating;
    int soldQuantity;
    int discountPercent;
    String status;
    Instant createdAt;
    Instant updatedAt;
    List<GenreInfo> genres;
    List<ImageInfo> images;

    @Data
    @AllArgsConstructor
    public static class GenreInfo {
        Integer id;
        String name;
    }

    @Data
    @AllArgsConstructor
    public static class ImageInfo {
        Integer id;
        String urlImage;
        boolean isIcon;
    }
}