package com.hungjava.bookstore.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateProfileRequest {
    String fullName;
    String phone;
    LocalDate dob;
    String gender;
    String billingAddress;
    String shippingAddress;
    String avatar;
}
