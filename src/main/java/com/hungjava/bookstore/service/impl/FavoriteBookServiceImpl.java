package com.hungjava.bookstore.service.impl;

import com.hungjava.bookstore.dto.response.FavoriteBookResponse;
import com.hungjava.bookstore.entity.Book;
import com.hungjava.bookstore.entity.FavoriteBook;
import com.hungjava.bookstore.entity.User;
import com.hungjava.bookstore.exception.ApiException;
import com.hungjava.bookstore.exception.ErrorCode;
import com.hungjava.bookstore.mapper.FavoriteBookMapper;
import com.hungjava.bookstore.repository.BookRepository;
import com.hungjava.bookstore.repository.FavoriteBookRepository;
import com.hungjava.bookstore.repository.UserRepository;
import com.hungjava.bookstore.service.FavoriteBookService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FavoriteBookServiceImpl implements FavoriteBookService {
    FavoriteBookRepository favoriteBookRepository;
    BookRepository bookRepository;
    UserRepository userRepository;
    FavoriteBookMapper favoriteBookMapper;

    @Override
    @Transactional
    public void addFavorite(int userId, int bookId) {
        if (favoriteBookRepository.existsByUserIdAndBookId(userId, bookId)) {
            return;
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        FavoriteBook favoriteBook = FavoriteBook.builder()
                .book(book)
                .user(user)
                .build();

        favoriteBookRepository.save(favoriteBook);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FavoriteBookResponse> getListFavorite(int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        List<FavoriteBook> favoriteBooks = favoriteBookRepository
                                                .findByUserIdOrderByCreatedAtDesc(userId);
        return favoriteBooks.stream()
                .map(favoriteBookMapper::toFavoriteBookResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFavorite(int userId, int bookId) {
        return favoriteBookRepository.existsByUserIdAndBookId(userId, bookId);
    }

    @Override
    @Transactional
    public void removeFavorite(int userId, int bookId) {
        favoriteBookRepository.deleteByUserIdAndBookId(userId, bookId);
    }
}

