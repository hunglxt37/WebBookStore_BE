package com.hungjava.bookstore.service.impl;

import com.hungjava.bookstore.dto.response.UserResponse;
import com.hungjava.bookstore.entity.User;
import com.hungjava.bookstore.exception.ApiException;
import com.hungjava.bookstore.exception.ErrorCode;
import com.hungjava.bookstore.mapper.UserMapper;
import com.hungjava.bookstore.repository.UserRepository;
import com.hungjava.bookstore.service.UserService;
import com.hungjava.bookstore.utils.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import com.hungjava.bookstore.dto.request.UpdateProfileRequest;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {
    UserRepository userRepository;
    UserMapper userMapper;

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    public UserResponse getMyProfile() {
        String username = SecurityUtils.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        return userMapper.toUserResponse(user);
    }

    @Override
    public UserResponse updateMyProfile(UpdateProfileRequest request) {
        String username = SecurityUtils.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getDob() != null) user.setDob(request.getDob());
        if (request.getGender() != null) user.setGender(request.getGender());
        if (request.getBillingAddress() != null) user.setBillingAddress(request.getBillingAddress());
        if (request.getShippingAddress() != null) user.setShippingAddress(request.getShippingAddress());
        if (request.getAvatar() != null) user.setAvatar(request.getAvatar());

        User savedUser = userRepository.save(user);
        return userMapper.toUserResponse(savedUser);
    }
}

