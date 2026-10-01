package com.hungjava.bookstore.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hungjava.bookstore.dto.ApiResponse;
import com.hungjava.bookstore.dto.response.FavoriteBookResponse;
import com.hungjava.bookstore.service.FavoriteBookService;
import com.hungjava.bookstore.utils.SecurityUtils;
import org.springframework.security.access.prepost.PreAuthorize;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController 
@RequestMapping ("${api.prefix}/favorite-books") 
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true) 
@PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
public class FavoriteBookController {

    FavoriteBookService favoriteBookService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<FavoriteBookResponse>>> listFavoriteBooks(){
        int userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.<List<FavoriteBookResponse>>builder()
                .success(true)
                .data(favoriteBookService.getListFavorite(userId))
                .build());
    }

    @PostMapping("/{bookId}")
    public ResponseEntity<ApiResponse<Void>> addToFavorite(@PathVariable int bookId) {
        int userId = SecurityUtils.getCurrentUserId();
        favoriteBookService.addFavorite(userId, bookId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Đã thêm vào danh sách yêu thích")
                .build());
    }

    @DeleteMapping("/{bookId}")
    public ResponseEntity<ApiResponse<Void>> removeFavorite(@PathVariable int bookId) {
        int userId = SecurityUtils.getCurrentUserId();
        favoriteBookService.removeFavorite(userId, bookId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Đã xóa khỏi danh sách yêu thích")
                .build());
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<ApiResponse<Boolean>> isFavorite(@PathVariable int bookId) {
        int userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.<Boolean>builder()
                .success(true)
                .data(favoriteBookService.isFavorite(userId, bookId))
                .build());
    }   
 
}
