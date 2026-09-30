package com.hungjava.bookstore.repository;

import java.math.BigDecimal;
import java.time.Instant;

public interface BookProjection {
    Integer getId();
    String getName();
    String getAuthor();
    BigDecimal getSellPrice();
    BigDecimal getListPrice();
    Integer getQuantity();
    Integer getDiscountPercent();
    String getStatus();
    Double getAvgRating();
    Integer getSoldQuantity();
    String getThumbnailUrl();
    String getGenreNames();
    Instant getCreatedAt();
    Instant getUpdatedAt();
}
