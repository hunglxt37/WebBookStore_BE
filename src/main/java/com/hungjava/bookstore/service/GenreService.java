package com.hungjava.bookstore.service;

import com.hungjava.bookstore.dto.PageResponse;
import com.hungjava.bookstore.dto.request.GenreRequest;
import com.hungjava.bookstore.dto.response.GenreResponse;

import java.util.List;

public interface GenreService {
    GenreResponse create(GenreRequest request);

    GenreResponse update(int id, GenreRequest request);

    void delete(int id);

    GenreResponse getById(int id);

    PageResponse<GenreResponse> getAll(int page, int size);

    List<GenreResponse> getAllNoPage();
}
