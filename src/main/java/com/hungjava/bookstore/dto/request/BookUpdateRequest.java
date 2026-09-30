package com.hungjava.bookstore.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookUpdateRequest {
    @NotBlank(message = "Tên sách không được để trống")
    String name;

    @NotBlank(message = "Tên tác giả không được để trống")
    String author;

    String description;

    @NotNull(message = "Giá niêm yết không được để trống")
    @Min(value = 0, message = "Giá niêm yết không được nhỏ hơn 0")
    BigDecimal listPrice;

    @NotNull(message = "Giá bán không được để trống")
    @Min(value = 0, message = "Giá bán không được nhỏ hơn 0")
    BigDecimal sellPrice;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 0, message = "Số lượng nhập kho không được nhỏ hơn 0")
    Integer quantity;

    @Min(value = 0, message = "Phần trăm giảm giá không được nhỏ hơn 0")
    @Max(value = 100, message = "Phần trăm giảm giá không được vượt quá 100%")
    Integer discountPercent;

    String status;

    List<Integer> genreIds;

    List<MultipartFile> images;
}