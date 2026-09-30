package com.hungjava.bookstore.service.impl;

import com.hungjava.bookstore.dto.security.*;
import com.hungjava.bookstore.entity.InvalidToken;
import com.hungjava.bookstore.entity.Role;
import com.hungjava.bookstore.entity.User;
import com.hungjava.bookstore.exception.ApiException;
import com.hungjava.bookstore.exception.ErrorCode;
import com.hungjava.bookstore.repository.InvalidTokenRepository;
import com.hungjava.bookstore.repository.RoleRepository;
import com.hungjava.bookstore.repository.UserRepository;
import com.hungjava.bookstore.service.AuthenticationService;
import com.hungjava.bookstore.service.EmailService;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;

@Service // Đánh dấu là một Service class (Logic nghiệp vụ)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationServiceImpl implements AuthenticationService {

    UserRepository userRepository;
    InvalidTokenRepository invalidTokenRepository; // Truy cập bảng Blacklist
    RoleRepository roleRepository;
    EmailService emailService;

    @Value("${jwt.signerKey}") // Lấy khóa bí mật
    @NonFinal
    String SIGNER_KEY;

    @Value("${app.activation-url}")
    @NonFinal
    String ACTIVATION_URL;

    // 0. HÀM ĐĂNG KÝ (REGISTER)
    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ApiException(ErrorCode.USER_ALREADY_EXISTS);
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        Role defaultRole = roleRepository.findByName("ROLE_CUSTOMER")
                .orElseThrow(() -> new RuntimeException("Quyền ROLE_CUSTOMER không tồn tại trong hệ thống"));

        // Sinh chuỗi mã kích hoạt
        String hashActive = UUID.randomUUID().toString();

        // Tạo đối tượng User mới
        User newUser = User.builder()
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .username(request.getUsername())
                .password(hashedPassword)
                .email(request.getEmail())
                .dob(request.getDob())
                .gender(request.getGender())
                .status("INACTIVE")
                .hashActive(hashActive)
                .roles(List.of(defaultRole))
                .build();

        userRepository.save(newUser);

        String activationLink = ACTIVATION_URL + "/activate?token=" + hashActive;
        emailService.sendActivationEmail(request.getEmail(),request.getFullName(), activationLink);

        return AuthResponse.builder()
                .message("Đăng ký thành công. Vui lòng kiểm tra email để kích hoạt tài khoản.")
                .status("INACTIVE")
                .build();
    }

    @Override
    public MessageResponse activateAccount(String token) {

        User user = userRepository.findByHashActive(token)
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_ACTIVATION_TOKEN));

        user.setStatus("ACTIVE");
        user.setHashActive(null);
        userRepository.save(user);

        return MessageResponse.builder()
                .message("Tài khoản của bạn đã được kích hoạt thành công!")
                .build();
    }

    @Override
    public AuthenticationResponse login(AuthenticationRequest authenticationRequest) {
        User user = userRepository.findByEmail(authenticationRequest.getEmail()).orElse(null);

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        if (user == null || !passwordEncoder.matches(authenticationRequest.getPassword(), user.getPassword())) {
            throw new ApiException(ErrorCode.INVALID_PASSWORD); // Email hoặc mật khẩu không chính xác
        }

        if (!"ACTIVE".equals(user.getStatus())) {
            throw new ApiException(ErrorCode.USER_INACTIVE);
        }

        String accessToken = generateToken(user, 1, ChronoUnit.HOURS, "ACCESS");
        String refreshToken = generateToken(user, 14, ChronoUnit.DAYS, "REFRESH");

        return AuthenticationResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    @Transactional
    public AuthenticationResponse refreshToken(RefreshTokenRequest request) throws ParseException, JOSEException {
        SignedJWT signedJWT = verifyToken(request.getRefreshToken());

        String tokenType = (String) signedJWT.getJWTClaimsSet().getClaim("tokenType");
        if (!"REFRESH".equals(tokenType)) {
            throw new JwtException("Token không phải là refresh token hợp lệ");
        }

        // Token Rotation: Vô hiệu hóa refresh token cũ ngay lập tức để chống tấn công replay
        String jwtId = signedJWT.getJWTClaimsSet().getJWTID();
        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        invalidTokenRepository.save(InvalidToken.builder()
                .id(jwtId)
                .expiryTime(new java.sql.Timestamp(expiryTime.getTime()).toLocalDateTime())
                .build());

        String username = signedJWT.getJWTClaimsSet().getSubject();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        if (!"ACTIVE".equals(user.getStatus())) {
            throw new ApiException(ErrorCode.USER_INACTIVE);
        }

        String newAccessToken = generateToken(user, 1, ChronoUnit.HOURS, "ACCESS");
        String newRefreshToken = generateToken(user, 14, ChronoUnit.DAYS, "REFRESH");

        return AuthenticationResponse.builder()
                .token(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    @Override
    public IntrospectResponse introspect(IntrospectRequest introspectRequest)  {
        try {
            SignedJWT jwt = verifyToken(introspectRequest.getToken());
            String tokenType = (String) jwt.getJWTClaimsSet().getClaim("tokenType");
            if (tokenType != null && !"ACCESS".equals(tokenType)) {
                return IntrospectResponse.builder().valid(false).build();
            }
            return IntrospectResponse.builder().valid(true).build();
        } catch (JwtException | ParseException e) {
            return IntrospectResponse.builder().valid(false).build();
        }
    }

    @Override
    public void logout(LogoutRequest logoutRequest) throws ParseException {
        try {
            // Giải mã token để lấy thông tin bên trong
            SignedJWT signedJWT = verifyToken(logoutRequest.getToken());

            // Rút trích ID duy nhất của token (JTI)
            String jwtId = signedJWT.getJWTClaimsSet().getJWTID();

            // Rút trích thời gian hết hạn của token
            Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

            // Tống ID token này vào bảng InvalidToken (Blacklist) trong Database
            invalidTokenRepository.save(InvalidToken.builder()
                    .id(jwtId)
                    // Convert Date sang LocalDateTime để lưu DB cho chuẩn
                    .expiryTime(new java.sql.Timestamp(expiryTime.getTime()).toLocalDateTime())
                    .build());
        } catch (JwtException e) {
            // Đã hết hạn hoặc không hợp lệ sẵn rồi thì bỏ qua không cần xử lý nữa
        }
    }


    // Tạo chuỗi JWT mới cho một User với loại token và thời hạn tương ứng
    private String generateToken(User user, long duration, ChronoUnit unit, String tokenType) {

        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("bookstore.com")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(duration, unit).toEpochMilli()
                ))
                .claim("userId", user.getId())
                .claim("tokenType", tokenType)
                .jwtID(UUID.randomUUID().toString());

        if ("ACCESS".equals(tokenType)) {
            builder.claim("role", getRoles(user));
        }

        Payload payload = new Payload(builder.build().toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException("Không thể tạo JWT", e);
        }
    }

    private String getRoles(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        user.getRoles().forEach(role ->
                stringJoiner.add(role.getName()));
        return stringJoiner.toString();
    }

    // Hàm lõi: Kiểm tra tính hợp lệ của token
    private SignedJWT verifyToken(String token) {
        try {
            // Khởi tạo công cụ xác minh chữ ký bằng Khóa bí mật
            JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
            // Đọc token từ chuỗi văn bản
            SignedJWT signedJWT = SignedJWT.parse(token);
            // Lấy thời gian hết hạn
            Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            // Yêu cầu Nimbus kiểm tra chữ ký có đúng do Server mình ký không
            var verified = signedJWT.verify(verifier);

            // Báo lỗi nếu Sai chữ ký HOẶC đã quá hạn thời gian
            if (!verified || expiryTime.before(new Date())) {
                throw new JwtException("Token không hợp lệ hoặc đã hết hạn");
            }

            // Báo lỗi tiếp nếu Token này đã từng bị Logout (nằm trong DB Blacklist)
            String jwtId = signedJWT.getJWTClaimsSet().getJWTID();
            if (invalidTokenRepository.findById(jwtId).isPresent()) {
                throw new JwtException("Token đã bị thu hồi");
            }

            return signedJWT;
        } catch (JOSEException | ParseException e) {
            throw new JwtException("Lỗi cấu trúc token");
        }
    }
}
