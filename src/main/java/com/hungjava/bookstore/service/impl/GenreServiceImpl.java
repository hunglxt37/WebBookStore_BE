package com.hungjava.bookstore.service.impl;

import com.hungjava.bookstore.dto.PageResponse;
import com.hungjava.bookstore.dto.request.GenreRequest;
import com.hungjava.bookstore.dto.response.GenreResponse;
import com.hungjava.bookstore.entity.Genre;
import com.hungjava.bookstore.exception.ApiException;
import com.hungjava.bookstore.exception.ErrorCode;
import com.hungjava.bookstore.mapper.GenreMapper;
import com.hungjava.bookstore.repository.GenreRepository;
import com.hungjava.bookstore.service.GenreService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GenreServiceImpl implements GenreService {
    GenreRepository genreRepository;
    GenreMapper genreMapper;

    @Override
    public GenreResponse create(GenreRequest request) {
        Genre genre = Genre.builder()
                .name(request.getName())
                .build();
        genre = genreRepository.save(genre);
        return genreMapper.toGenreResponse(genre);
    }

    @Override
    public GenreResponse update(int id, GenreRequest request) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.GENRE_NOT_FOUND));
        genre.setName(request.getName());
        genre = genreRepository.save(genre);
        return genreMapper.toGenreResponse(genre);
    }

    @Override
    public void delete(int id) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.GENRE_NOT_FOUND));
        genreRepository.delete(genre);
    }

    @Override
    public GenreResponse getById(int id) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.GENRE_NOT_FOUND));
        return genreMapper.toGenreResponse(genre);
    }

    @Override
    public PageResponse<GenreResponse> getAll(int page, int size) {
        Page<Genre> pageData = genreRepository.findAll(PageRequest.of(page, size));
        Page<GenreResponse> responsePage = pageData.map(genreMapper::toGenreResponse);
        return new PageResponse<>(responsePage);
    }

    @Override
    public java.util.List<GenreResponse> getAllNoPage() {
        return genreRepository.findAll().stream()
                .map(genreMapper::toGenreResponse)
                .toList();
    }
}
