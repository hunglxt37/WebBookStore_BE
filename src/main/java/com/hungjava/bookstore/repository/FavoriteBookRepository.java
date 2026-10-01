package com.hungjava.bookstore.repository;

import com.hungjava.bookstore.entity.FavoriteBook;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface FavoriteBookRepository extends JpaRepository<FavoriteBook, Integer> {

    // Kiểm tra sách này user đã thích chưa
    boolean existsByUserIdAndBookId(int userId, int bookId);
    
    @Modifying
    @Query("DELETE FROM FavoriteBook f WHERE f.book.id = :bookId")
    void deleteByBookId(@Param("bookId") Integer bookId);

    // Xóa sách khỏi danh sách yêu thích của user
    @Modifying
    @Query("DELETE FROM FavoriteBook f WHERE f.user.id = :userId AND f.book.id = :bookId")
    void deleteByUserIdAndBookId(@Param("userId") int userId, @Param("bookId") int bookId);
    // Lấy toàn bộ sách yêu thích của user, sắp xếp mới nhất lên đầu
    List<FavoriteBook> findByUserIdOrderByCreatedAtDesc(int userId);


}
