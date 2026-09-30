package com.hungjava.bookstore.service.impl;

import com.hungjava.bookstore.dto.PageResponse;
import com.hungjava.bookstore.dto.request.CreateReviewRequest;
import com.hungjava.bookstore.dto.response.ReviewResponse;
import com.hungjava.bookstore.dto.response.ReviewSummaryResponse;
import com.hungjava.bookstore.entity.Book;
import com.hungjava.bookstore.entity.OrderDetail;
import com.hungjava.bookstore.entity.Review;
import com.hungjava.bookstore.exception.ApiException;
import com.hungjava.bookstore.exception.ErrorCode;
import com.hungjava.bookstore.repository.BookRepository;
import com.hungjava.bookstore.repository.OrderDetailRepository;
import com.hungjava.bookstore.repository.ReviewRepository;
import com.hungjava.bookstore.service.ReviewService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReviewServiceImpl implements ReviewService {

    ReviewRepository reviewRepository;
    OrderDetailRepository orderDetailRepository;
    BookRepository bookRepository;

    @Override
    @Transactional
    public ReviewResponse createReview(int userId, CreateReviewRequest request) {
        OrderDetail orderDetail = orderDetailRepository.findById(request.getOrderDetailId())
                .orElseThrow(() -> new ApiException(ErrorCode.ORDER_DETAIL_NOT_FOUND));

        if (orderDetail.getOrder().getUser().getId() != userId) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }

        if (!"DELIVERED".equalsIgnoreCase(orderDetail.getOrder().getStatus())) {
            throw new ApiException(ErrorCode.ORDER_NOT_DELIVERED);
        }

        if (orderDetail.isReview() || reviewRepository.existsByOrderDetailId(orderDetail.getId())) {
            throw new ApiException(ErrorCode.ALREADY_REVIEWED);
        }

        Review review = Review.builder()
                .content(request.getContent().trim())
                .ratingPoint(request.getRatingPoint())
                .book(orderDetail.getBook())
                .user(orderDetail.getOrder().getUser())
                .orderDetail(orderDetail)
                .build();
        Review savedReview = reviewRepository.save(review);

        orderDetail.setReview(true);
        orderDetailRepository.save(orderDetail);

        updateBookAverageRating(orderDetail.getBook());

        return mapToResponse(savedReview);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> getReviewsByBookId(int bookId, int page, int size) {
        if (!bookRepository.existsById(bookId)) {
            throw new ApiException(ErrorCode.BOOK_NOT_FOUND);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Review> reviewPage = reviewRepository.findByBookIdOrderByCreatedAtDesc(bookId, pageable);
        Page<ReviewResponse> responsePage = reviewPage.map(this::mapToResponse);

        return new PageResponse<>(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewSummaryResponse getReviewSummary(int bookId) {
        if (!bookRepository.existsById(bookId)) {
            throw new ApiException(ErrorCode.BOOK_NOT_FOUND);
        }

        Double avg = reviewRepository.getAverageRatingByBookId(bookId);
        double roundedAvg = avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
        long total = reviewRepository.countByBookId(bookId);
        long star5 = reviewRepository.countByBookIdAndRatingPoint(bookId, 5.0f);
        long star4 = reviewRepository.countByBookIdAndRatingPoint(bookId, 4.0f);
        long star3 = reviewRepository.countByBookIdAndRatingPoint(bookId, 3.0f);
        long star2 = reviewRepository.countByBookIdAndRatingPoint(bookId, 2.0f);
        long star1 = reviewRepository.countByBookIdAndRatingPoint(bookId, 1.0f);

        return ReviewSummaryResponse.builder()
                .avgRating(roundedAvg)
                .totalReviews(total)
                .star5Count(star5)
                .star4Count(star4)
                .star3Count(star3)
                .star2Count(star2)
                .star1Count(star1)
                .build();
    }

    @Override
    @Transactional
    public void deleteReview(int reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ApiException(ErrorCode.REVIEW_NOT_FOUND));

        Book book = review.getBook();
        OrderDetail orderDetail = review.getOrderDetail();
        if (orderDetail != null) {
            orderDetail.setReview(false);
            orderDetailRepository.save(orderDetail);
        }

        reviewRepository.delete(review);
        updateBookAverageRating(book);
    }

    private void updateBookAverageRating(Book book) {
        Double avg = reviewRepository.getAverageRatingByBookId(book.getId());
        double roundedAvg = avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
        book.setAvgRating(roundedAvg);
        bookRepository.save(book);
    }

    private ReviewResponse mapToResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .bookId(review.getBook() != null ? review.getBook().getId() : null)
                .bookName(review.getBook() != null ? review.getBook().getName() : null)
                .userId(review.getUser() != null ? review.getUser().getId() : null)
                .userFullName(review.getUser() != null ? review.getUser().getFullName() : "Khách hàng")
                .userAvatar(review.getUser() != null ? review.getUser().getAvatar() : null)
                .ratingPoint(review.getRatingPoint())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
