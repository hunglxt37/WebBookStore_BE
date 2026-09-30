package com.hungjava.bookstore.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewResponse {
    int id;
    int bookId;
    String bookName;
    int userId;
    String userFullName;
    String userAvatar;
    float ratingPoint;
    String content;
    Instant createdAt;
    Instant updatedAt;
}
