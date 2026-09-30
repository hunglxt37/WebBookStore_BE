package com.hungjava.bookstore.repository;

import com.hungjava.bookstore.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer> {

        @Query(value = "SELECT b.id AS id, " +
                        "b.name AS name, " +
                        "b.author AS author, " +
                        "b.list_price AS listPrice, " +
                        "b.sell_price AS sellPrice, " +
                        "b.quantity AS quantity, " +
                        "b.discount_percent AS discountPercent, " +
                        "b.status AS status, " +
                        "b.avg_rating AS avgRating, " +
                        "b.sold_quantity AS soldQuantity, " +
                        "i.url_image AS thumbnailUrl, " +
                        "b.created_at AS createdAt, " +
                        "b.updated_at AS updatedAt, " +
                        "STRING_AGG(g.name, ', ') AS genreNames " +
                        "FROM books b " +
                        "LEFT JOIN images i ON b.id = i.book_id AND i.is_icon = true " +
                        "LEFT JOIN book_genre bg ON b.id = bg.book_id " +
                        "LEFT JOIN genres g ON bg.genre_id = g.id " +
                        "WHERE (:search IS NULL OR b.name ILIKE :search OR b.author ILIKE :search) " +
                        "  AND (:genreId IS NULL OR b.id IN (SELECT book_id FROM book_genre WHERE genre_id = :genreId)) "
                        +
                        "  AND (:minPrice IS NULL OR b.sell_price >= :minPrice) " +
                        "  AND (:maxPrice IS NULL OR b.sell_price <= :maxPrice) " +
                        "  AND (:rating IS NULL OR (b.avg_rating >= :rating AND b.avg_rating < :rating + 1)) " +
                        "GROUP BY b.id, i.url_image", countQuery = "SELECT COUNT(DISTINCT b.id) " +
                                        "FROM books b " +
                                        "LEFT JOIN book_genre bg ON b.id = bg.book_id " +
                                        "WHERE (:search IS NULL OR b.name ILIKE :search OR b.author ILIKE :search) " +
                                        "  AND (:genreId IS NULL OR bg.genre_id = :genreId) " +
                                        "  AND (:minPrice IS NULL OR b.sell_price >= :minPrice) " +
                                        "  AND (:maxPrice IS NULL OR b.sell_price <= :maxPrice) " +
                                        "  AND (:rating IS NULL OR (b.avg_rating >= :rating AND b.avg_rating < :rating + 1))", nativeQuery = true)
        Page<BookProjection> findAllBooksWithFilters(
                        @Param("search") String search,
                        @Param("genreId") Integer genreId,
                        @Param("minPrice") BigDecimal minPrice,
                        @Param("maxPrice") BigDecimal maxPrice,
                        @Param("rating") Integer rating,
                        Pageable pageable);
}
