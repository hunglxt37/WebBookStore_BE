package com.hungjava.bookstore.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewSummaryResponse {
    double avgRating;
    long totalReviews;
    long star5Count;
    long star4Count;
    long star3Count;
    long star2Count;
    long star1Count;
}
