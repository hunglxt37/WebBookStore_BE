package com.hungjava.bookstore.controller;

import com.hungjava.bookstore.dto.ApiResponse;
import com.hungjava.bookstore.dto.PageResponse;
import com.hungjava.bookstore.dto.request.CreateReviewRequest;
import com.hungjava.bookstore.dto.response.ReviewResponse;
import com.hungjava.bookstore.dto.response.ReviewSummaryResponse;
import com.hungjava.bookstore.service.ReviewService;
import com.hungjava.bookstore.utils.SecurityUtils;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/reviews")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReviewController {

    ReviewService reviewService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @Valid @RequestBody CreateReviewRequest request) {
        int userId = SecurityUtils.getCurrentUserId();
        ReviewResponse response = reviewService.createReview(userId, request);

        return ResponseEntity.status(201).body(ApiResponse.<ReviewResponse>builder()
                .success(true)
                .data(response)
                .build());
    }

    @GetMapping("/book/{bookId}")
    public ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> getReviewsByBook(
            @PathVariable int bookId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        PageResponse<ReviewResponse> response = reviewService.getReviewsByBookId(bookId, page, size);

        return ResponseEntity.ok(ApiResponse.<PageResponse<ReviewResponse>>builder()
                .success(true)
                .data(response)
                .build());
    }

    @GetMapping("/book/{bookId}/summary")
    public ResponseEntity<ApiResponse<ReviewSummaryResponse>> getReviewSummary(
            @PathVariable int bookId) {
        ReviewSummaryResponse response = reviewService.getReviewSummary(bookId);

        return ResponseEntity.ok(ApiResponse.<ReviewSummaryResponse>builder()
                .success(true)
                .data(response)
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable int id) {
        reviewService.deleteReview(id);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .build());
    }
}
