package com.hungjava.bookstore.service;

import java.util.List;

import com.hungjava.bookstore.dto.response.FavoriteBookResponse;

public interface FavoriteBookService {

    // Kiểm tra sách này user đã thích chưa
    boolean isFavorite(int userId, int bookId);

    // Thêm sách vào danh sách yêu thích (khi click icon tim)
    void addFavorite(int userId, int bookId);

    // Xóa sách khỏi danh sách yêu thích (khi click icon tim lần 2)
    void removeFavorite(int userId, int bookId);

    // Lấy toàn bộ sách yêu thích của user, sắp xếp mới nhất lên đầu
    List<FavoriteBookResponse> getListFavorite(int userId);
}
