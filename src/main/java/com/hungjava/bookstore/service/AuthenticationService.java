package com.hungjava.bookstore.service;

import com.hungjava.bookstore.dto.security.*;
import com.nimbusds.jose.JOSEException;
import java.text.ParseException;

public interface AuthenticationService {
    AuthResponse register(RegisterRequest request);
    MessageResponse activateAccount(String token);
    AuthenticationResponse login(AuthenticationRequest authenticationRequest);
    AuthenticationResponse refreshToken(RefreshTokenRequest request) throws ParseException, JOSEException;
    IntrospectResponse introspect(IntrospectRequest introspectRequest);
    void logout(LogoutRequest logoutRequest) throws ParseException;
}
