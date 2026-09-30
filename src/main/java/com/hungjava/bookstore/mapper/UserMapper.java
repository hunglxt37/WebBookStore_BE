package com.hungjava.bookstore.mapper;

import com.hungjava.bookstore.dto.response.UserResponse;
import com.hungjava.bookstore.entity.User;
import com.hungjava.bookstore.entity.Role;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class UserMapper {

    public UserResponse toUserResponse(User user) {
        if (user == null) {
            return null;
        }
        List<String> roles = user.getRoles() != null
                ? user.getRoles().stream().map(Role::getName).toList()
                : Collections.emptyList();

        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .phone(user.getPhone())
                .dob(user.getDob())
                .gender(user.getGender())
                .billingAddress(user.getBillingAddress())
                .shippingAddress(user.getShippingAddress())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .roles(roles)
                .build();
    }
}
