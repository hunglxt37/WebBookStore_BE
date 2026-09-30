package com.hungjava.bookstore.mapper;

import com.hungjava.bookstore.dto.request.BookCreateRequest;
import com.hungjava.bookstore.dto.response.BookListResponse;
import com.hungjava.bookstore.dto.response.BookResponse;
import com.hungjava.bookstore.entity.Book;
import com.hungjava.bookstore.entity.Genre;
import com.hungjava.bookstore.repository.BookProjection;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BookMapper {

    public Book mapToBook(BookCreateRequest request) {
        if (request == null) {
            return null;
        }
        return Book.builder()
                .name(request.getName())
                .author(request.getAuthor())
                .description(request.getDescription())
                .listPrice(request.getListPrice())
                .sellPrice(request.getSellPrice())
                .quantity(request.getQuantity() != null ? request.getQuantity() : 0)
                .discountPercent(request.getDiscountPercent() != null ? request.getDiscountPercent() : 0)
                .avgRating(0.0)
                .soldQuantity(0)
                .build();
    }

    public Book mapToBook(BookCreateRequest request, List<Genre> genres) {
        if (request == null) {
            return null;
        }
        return Book.builder()
                .name(request.getName())
                .author(request.getAuthor())
                .description(request.getDescription())
                .listPrice(request.getListPrice())
                .sellPrice(request.getSellPrice())
                .quantity(request.getQuantity() != null ? request.getQuantity() : 0)
                .discountPercent(request.getDiscountPercent() != null ? request.getDiscountPercent() : 0)
                .avgRating(0.0)
                .soldQuantity(0)
                .listGenres(genres)
                .build();
    }

    public BookResponse toBookResponse(Book book) {
        if (book == null) {
            return null;
        }

        List<BookResponse.GenreInfo> genreInfoResponses = null;
        if (book.getListGenres() != null) {
            genreInfoResponses = book.getListGenres().stream()
                    .map(genre -> new BookResponse.GenreInfo(genre.getId(), genre.getName()))
                    .toList();
        }

        List<BookResponse.ImageInfo> imageInfoResponses = null;
        if (book.getListImages() != null) {
            imageInfoResponses = book.getListImages().stream()
                    .map(img -> new BookResponse.ImageInfo(img.getId(), img.getUrlImage(), img.isIcon()))
                    .toList();
        }

        return BookResponse.builder()
                .id(book.getId())
                .name(book.getName())
                .author(book.getAuthor())
                .description(book.getDescription())
                .listPrice(book.getListPrice())
                .sellPrice(book.getSellPrice())
                .quantity(book.getQuantity())
                .avgRating(book.getAvgRating())
                .soldQuantity(book.getSoldQuantity())
                .discountPercent(book.getDiscountPercent())
                .status(book.getStatus())
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .genres(genreInfoResponses)
                .images(imageInfoResponses)
                .build();
    }

    public BookListResponse toBookListResponse(BookProjection projection) {
        if (projection == null) {
            return null;
        }

        List<String> genreList = null;
        if (projection.getGenreNames() != null && !projection.getGenreNames().isBlank()) {
            genreList = List.of(projection.getGenreNames().split(", "));
        }

        return BookListResponse.builder()
                .id(projection.getId())
                .name(projection.getName())
                .author(projection.getAuthor())
                .listPrice(projection.getListPrice())
                .sellPrice(projection.getSellPrice())
                .discountPercent(projection.getDiscountPercent() != null ? projection.getDiscountPercent() : 0)
                .quantity(projection.getQuantity() != null ? projection.getQuantity() : 0)
                .status(projection.getStatus())
                .avgRating(projection.getAvgRating() != null ? projection.getAvgRating() : 0.0)
                .soldQuantity(projection.getSoldQuantity() != null ? projection.getSoldQuantity() : 0)
                .thumbnailUrl(projection.getThumbnailUrl())
                .genres(genreList)
                .createdAt(projection.getCreatedAt())
                .updatedAt(projection.getUpdatedAt())
                .build();
    }
}
