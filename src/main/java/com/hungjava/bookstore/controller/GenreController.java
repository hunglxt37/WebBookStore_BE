package com.hungjava.bookstore.controller;

import com.hungjava.bookstore.dto.ApiResponse;
import com.hungjava.bookstore.dto.PageResponse;
import com.hungjava.bookstore.dto.request.GenreRequest;
import com.hungjava.bookstore.dto.response.GenreResponse;
import com.hungjava.bookstore.service.GenreService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("${api.prefix}/genres")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GenreController {
    GenreService genreService;

    @PostMapping
    public ResponseEntity<ApiResponse<GenreResponse>> create(@Valid @RequestBody GenreRequest request) {
        return ResponseEntity.status(201).body(ApiResponse.<GenreResponse>builder()
                .success(true)
                .data(genreService.create(request))
                .build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<GenreResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.<PageResponse<GenreResponse>>builder()
                .success(true)
                .data(genreService.getAll(page, size))
                .build());
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<GenreResponse>>> getAllNoPage() {
        return ResponseEntity.ok(ApiResponse.<List<GenreResponse>>builder()
                .success(true)
                .data(genreService.getAllNoPage())
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GenreResponse>> getById(@PathVariable int id) {
        return ResponseEntity.ok(ApiResponse.<GenreResponse>builder()
                .success(true)
                .data(genreService.getById(id))
                .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<GenreResponse>> update(@PathVariable int id,@Valid @RequestBody GenreRequest request) {
        return ResponseEntity.ok(ApiResponse.<GenreResponse>builder()
                .success(true)
                .data(genreService.update(id, request))
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable int id) {
        genreService.delete(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .build());
    }
}
