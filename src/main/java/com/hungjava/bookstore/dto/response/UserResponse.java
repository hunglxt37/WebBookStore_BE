package com.hungjava.bookstore.dto.response;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Value
@Builder
public class UserResponse {
    Integer id;
    String fullName;
    String username;
    String email;
    String avatar;
    String phone;
    LocalDate dob;
    String gender;
    String billingAddress;
    String shippingAddress;
    String status;
    Instant createdAt;
    List<String> roles;
}
