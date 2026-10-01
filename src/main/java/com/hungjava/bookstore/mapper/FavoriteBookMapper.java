package com.hungjava.bookstore.mapper;

import org.springframework.stereotype.Component;

import com.hungjava.bookstore.dto.response.FavoriteBookResponse;
import com.hungjava.bookstore.entity.FavoriteBook;
import com.hungjava.bookstore.entity.Image;

@Component 
public class FavoriteBookMapper {
    public FavoriteBookResponse toFavoriteBookResponse(FavoriteBook favoriteBook) {
        if (favoriteBook == null || favoriteBook.getBook() == null) {
            return null;
        }

        String thumbnailUrl = null;
        if (favoriteBook.getBook().getListImages() != null && !favoriteBook.getBook().getListImages().isEmpty()) {
            thumbnailUrl = favoriteBook.getBook().getListImages().stream()
                    .filter(Image::isIcon)  
                    .map(Image::getUrlImage)
                    .findFirst()
                    .orElse(favoriteBook.getBook().getListImages().get(0).getUrlImage());
        }

        return FavoriteBookResponse.builder()
                .id(favoriteBook.getId())
                .bookId(favoriteBook.getBook().getId())
                .name(favoriteBook.getBook().getName())
                .author(favoriteBook.getBook().getAuthor())
                .listPrice(favoriteBook.getBook().getListPrice())
                .sellPrice(favoriteBook.getBook().getSellPrice())
                .discountPercent(favoriteBook.getBook().getDiscountPercent())
                .quantity(favoriteBook.getBook().getQuantity())
                .avgRating(favoriteBook.getBook().getAvgRating())
                .thumbnailUrl(thumbnailUrl)
                .status(favoriteBook.getBook().getStatus())
                .addedAt(favoriteBook.getCreatedAt())
                .build();
    }
}
