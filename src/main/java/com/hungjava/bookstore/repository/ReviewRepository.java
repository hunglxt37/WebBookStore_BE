package com.hungjava.bookstore.repository;

import com.hungjava.bookstore.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    Page<Review> findByBookIdOrderByCreatedAtDesc(int bookId, Pageable pageable);

    Optional<Review> findByOrderDetailId(int orderDetailId);

    boolean existsByOrderDetailId(int orderDetailId);

    @Query("SELECT COALESCE(AVG(r.ratingPoint), 0.0) FROM Review r WHERE r.book.id = :bookId")
    Double getAverageRatingByBookId(@Param("bookId") int bookId);

    long countByBookId(int bookId);

    long countByBookIdAndRatingPoint(int bookId, float ratingPoint);
}
