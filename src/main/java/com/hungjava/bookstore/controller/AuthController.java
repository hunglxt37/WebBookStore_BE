package com.hungjava.bookstore.controller;

import com.hungjava.bookstore.dto.ApiResponse;
import com.hungjava.bookstore.dto.security.*;
import com.hungjava.bookstore.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("${api.prefix}/auth")
public class AuthController {

    AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(201).body(ApiResponse.<AuthResponse>builder()
                .success(true)
                .data(authenticationService.register(request))
                .build());
    }

    @GetMapping("/activate")
    public ResponseEntity<ApiResponse<MessageResponse>> activate(@RequestParam("token") String token) {
        return ResponseEntity.ok(ApiResponse.<MessageResponse>builder()
                .success(true)
                .data(authenticationService.activateAccount(token))
                .build());
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> authenticate(@Valid @RequestBody AuthenticationRequest authenticationRequest) {
        return ResponseEntity.ok(ApiResponse.<AuthenticationResponse>builder()
                .success(true)
                .data(authenticationService.login(authenticationRequest))
                .build());
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) throws ParseException, com.nimbusds.jose.JOSEException {
        return ResponseEntity.ok(ApiResponse.<AuthenticationResponse>builder()
                .success(true)
                .data(authenticationService.refreshToken(request))
                .build());
    }

    @PostMapping("/introspect")
    public ResponseEntity<ApiResponse<IntrospectResponse>> introspect(@Valid @RequestBody IntrospectRequest introspectRequest) {
        return ResponseEntity.ok(ApiResponse.<IntrospectResponse>builder()
                .success(true)
                .data(authenticationService.introspect(introspectRequest))
                .build());
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody LogoutRequest logoutRequest) throws ParseException {
        authenticationService.logout(logoutRequest);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .build());
    }
}
