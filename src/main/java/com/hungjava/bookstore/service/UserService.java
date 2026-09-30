package com.hungjava.bookstore.service;

import com.hungjava.bookstore.dto.response.UserResponse;
import com.hungjava.bookstore.entity.User;
import com.hungjava.bookstore.dto.request.UpdateProfileRequest;

public interface UserService {
    User findByUsername(String username);

    UserResponse getMyProfile();

    UserResponse updateMyProfile(UpdateProfileRequest request);
}

