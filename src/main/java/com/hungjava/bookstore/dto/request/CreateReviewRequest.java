package com.hungjava.bookstore.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateReviewRequest {

    @NotNull(message = "Mã chi tiết đơn hàng không được để trống")
    Integer orderDetailId;

    @NotNull(message = "Điểm đánh giá không được để trống")
    @DecimalMin(value = "1.0", message = "Điểm đánh giá tối thiểu là 1 sao")
    @DecimalMax(value = "5.0", message = "Điểm đánh giá tối đa là 5 sao")
    Float ratingPoint;

    @NotBlank(message = "Nội dung nhận xét không được để trống")
    @Size(max = 1000, message = "Nội dung nhận xét không vượt quá 1000 ký tự")
    String content;
}
