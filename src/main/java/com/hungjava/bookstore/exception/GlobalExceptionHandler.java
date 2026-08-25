package com.hungjava.bookstore.exception;

import com.hungjava.bookstore.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {



    // 1) Bắt lỗi Validation (MethodArgumentNotValidException)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> fieldErrors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        return ResponseEntity.badRequest().body(
                ApiResponse.<Map<String, String>>builder()
                        .success(false)
                        .data(fieldErrors)
                        .error(ApiResponse.ApiError.builder()
                                .code("VALIDATION_FAILED")
                                .message("Input validation failed")
                                .path(request.getRequestURI())
                                .build())
                        .build()
        );
    }

    // 2) Bắt ResponseStatusException (ném từ Service)
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApiException(
            ApiException ex,
            HttpServletRequest request) {

        ErrorCode errorCode = ex.getErrorCode();

        return ResponseEntity.status(ex.getStatusCode()).body(
                ApiResponse.<Void>builder()
                        .success(false)
                        .error(ApiResponse.ApiError.builder()
                                .code(errorCode.name())
                                .message(ex.getReason()) 
                                .path(request.getRequestURI())
                                .build())
                        .build()
        );
    }

    // 3) Bắt mọi exception không mong đợi (fallback)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneral(
            Exception ex,
            HttpServletRequest request) {

        // In ra lỗi thật vào console Spring Boot để debug
        log.error("[INTERNAL_ERROR] Unexpected exception at {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.<Void>builder()
                        .success(false)
                        .error(ApiResponse.ApiError.builder()
                                .code("INTERNAL_ERROR")
                                .message("An unexpected error occurred")
                                .path(request.getRequestURI())
                                .build())
                        .build()
        );
    }

}