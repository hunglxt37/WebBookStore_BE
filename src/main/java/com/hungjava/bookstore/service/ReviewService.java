package com.hungjava.bookstore.service;

import com.hungjava.bookstore.dto.PageResponse;
import com.hungjava.bookstore.dto.request.CreateReviewRequest;
import com.hungjava.bookstore.dto.response.ReviewResponse;
import com.hungjava.bookstore.dto.response.ReviewSummaryResponse;

public interface ReviewService {

    ReviewResponse createReview(int userId, CreateReviewRequest request);

    PageResponse<ReviewResponse> getReviewsByBookId(int bookId, int page, int size);

    ReviewSummaryResponse getReviewSummary(int bookId);

    void deleteReview(int reviewId);
}
