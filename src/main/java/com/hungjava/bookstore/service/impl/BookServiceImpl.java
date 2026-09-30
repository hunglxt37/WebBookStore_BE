package com.hungjava.bookstore.service.impl;

import com.hungjava.bookstore.dto.PageResponse;
import com.hungjava.bookstore.dto.request.BookCreateRequest;
import com.hungjava.bookstore.dto.request.BookUpdateRequest;
import com.hungjava.bookstore.dto.response.BookListResponse;
import com.hungjava.bookstore.dto.response.BookResponse;
import com.hungjava.bookstore.entity.Book;
import com.hungjava.bookstore.entity.Genre;
import com.hungjava.bookstore.entity.Image;
import com.hungjava.bookstore.exception.ApiException;
import com.hungjava.bookstore.exception.ErrorCode;
import com.hungjava.bookstore.mapper.BookMapper;
import com.hungjava.bookstore.repository.BookProjection;
import com.hungjava.bookstore.repository.BookRepository;
import com.hungjava.bookstore.repository.GenreRepository;
import com.hungjava.bookstore.repository.ImageRepository;
import com.hungjava.bookstore.service.BookService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import com.hungjava.bookstore.repository.OrderDetailRepository;
import com.hungjava.bookstore.repository.CartItemRepository;
import com.hungjava.bookstore.repository.FavoriteBookRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class BookServiceImpl implements BookService {

    BookRepository bookRepository;
    ImageRepository imageRepository;
    CloudinaryService cloudinaryService;
    GenreRepository genreRepository;
    BookMapper bookMapper;
    OrderDetailRepository orderDetailRepository;
    CartItemRepository cartItemRepository;
    FavoriteBookRepository favoriteBookRepository;

    @Override
    @Transactional
    public BookResponse addBook(BookCreateRequest request) {

        //Tìm danh sách thể loại từ DB dựa vào danh sách ID truyền lên từ request
        List<Genre> genres = new ArrayList<>();
        if (request.getGenreIds() != null && !request.getGenreIds().isEmpty()) {
            genres = genreRepository.findAllById(request.getGenreIds());
        }

        Book book = bookMapper.mapToBook(request, genres);
        book = bookRepository.save(book);

        //Tải danh sách ảnh lên Cloudinary và lưu thông tin ảnh vào bảng images
        List<Image> savedImages = new ArrayList<>();
        if (request.getImages() != null && !request.getImages().isEmpty()) {

            List<String> imageUrls = cloudinaryService.uploadMultipleFiles(request.getImages(), "books");

            List<Image> imagesToSave = new ArrayList<>();
            for (int i = 0; i < imageUrls.size(); i++) {
                imagesToSave.add(Image.builder()
                        .name("image_" + book.getId() + "_" + i)
                        .urlImage(imageUrls.get(i))
                        .isIcon(i == 0) // Đặt tấm ảnh đầu tiên làm ảnh đại diện chính (isIcon = true)
                        .book(book)
                        .build());
            }
            savedImages = imageRepository.saveAll(imagesToSave);
        }
        book.setListImages(savedImages);
        return bookMapper.toBookResponse(book);
    }

    @Override
    public PageResponse<BookListResponse> getAllBooks(
            String search,
            Integer genreId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Integer rating,
            Pageable pageable
    ) {
        String searchPattern = (search != null && !search.trim().isEmpty()) ? "%" + search.trim() + "%" : null;

        Page<BookProjection> pageData = bookRepository.findAllBooksWithFilters(
                searchPattern,
                genreId,
                minPrice,
                maxPrice,
                rating,
                pageable
        );
        Page<BookListResponse> responsePage = pageData.map(bookMapper::toBookListResponse);
        return new PageResponse<>(responsePage);
    }

    @Override
    public BookResponse getById(Integer id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(()-> new ApiException(ErrorCode.BOOK_NOT_FOUND));

        return bookMapper.toBookResponse(book);
    }

    @Override
    @Transactional
    public BookResponse updateBook(Integer id, BookUpdateRequest request) {
        Book book = bookRepository.findById(id)
                .orElseThrow(()-> new ApiException(ErrorCode.BOOK_NOT_FOUND));

        book.setName(request.getName());
        book.setAuthor(request.getAuthor());
        book.setDescription(request.getDescription());
        book.setListPrice(request.getListPrice());
        book.setSellPrice(request.getSellPrice());
        book.setQuantity(request.getQuantity());
        book.setDiscountPercent(request.getDiscountPercent() != null ? request.getDiscountPercent() : 0);
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            book.setStatus(request.getStatus());
        }

        book.getListGenres().clear();
        if (request.getGenreIds() != null && !request.getGenreIds().isEmpty()) {
            List<Genre> newGenres = genreRepository.findAllById(request.getGenreIds());
            book.getListGenres().addAll(newGenres);
        }

        //Xử lý ảnh
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            List<Image> oldImages = imageRepository.findByBookId(id);
            for (Image img : oldImages) {
                try {
                    String publicId = cloudinaryService.extractPublicIdFromUrl(img.getUrlImage());
                    if (publicId != null) {
                        cloudinaryService.deleteFile(publicId);
                    }
                } catch (Exception e) {
                    log.error("Xóa ảnh trên Cloudinary thất bại cho URL: {}", img.getUrlImage(), e);
                }
            }

            imageRepository.deleteByBookId(id);

            List<String> imageUrls = cloudinaryService.uploadMultipleFiles(request.getImages(), "books");

            List<Image> newImages = new ArrayList<>();
            for (int i = 0; i < imageUrls.size(); i++) {
                newImages.add(Image.builder()
                        .name("image_" + book.getId() + "_" + i)
                        .urlImage(imageUrls.get(i))
                        .isIcon(i == 0)
                        .book(book)
                        .build());
            }
            List<Image> savedImages = imageRepository.saveAll(newImages);
            book.setListImages(savedImages);
        }

        Book updatedBook = bookRepository.save(book);

        return bookMapper.toBookResponse(updatedBook);
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(()-> new ApiException(ErrorCode.BOOK_NOT_FOUND));

        boolean checkOrdered = orderDetailRepository.existsByBookId(id);
        if (checkOrdered) {
            book.setStatus("INACTIVE");
            bookRepository.save(book);
            return;
        }

        List<Image> bookImages = imageRepository.findByBookId(id);
        for (Image img : bookImages) {
            if (img.getUrlImage() != null) {
                try {
                    String publicId = cloudinaryService.extractPublicIdFromUrl(img.getUrlImage());
                    if (publicId != null) {
                        cloudinaryService.deleteFile(publicId);
                    }
                } catch (Exception e) {
                    log.error("Không thể xóa hình ảnh: {}", img.getUrlImage(), e);
                }
            }
        }

        imageRepository.deleteAll(bookImages);
        cartItemRepository.deleteByBookId(id);
        favoriteBookRepository.deleteByBookId(id);
        
        book.getListImages().clear();
        book.getListGenres().clear();
        
        bookRepository.delete(book);
    }
}

