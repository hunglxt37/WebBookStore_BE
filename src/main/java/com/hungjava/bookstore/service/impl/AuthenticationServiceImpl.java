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

        User user = userRepository.findByEmail(authenticationRequest.getEmail())
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_USER));

        if (!"ACTIVE".equals(user.getStatus())) {
            throw new ApiException(ErrorCode.USER_INACTIVE);
        }

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

        if(!passwordEncoder.matches(authenticationRequest.getPassword(), user.getPassword())) {
            throw new ApiException(ErrorCode.INVALID_PASSWORD);
        }

        return AuthenticationResponse.builder()
                .token(generateToken(user))
                .build();
    }

    @Override
    public IntrospectResponse introspect(IntrospectRequest introspectRequest)  {
        try {
            verifyToken(introspectRequest.getToken());
            return IntrospectResponse.builder().valid(true).build();
        } catch (JwtException e) {
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


    //Tạo chuỗi JWT mới cho một User
    private String generateToken(User user) {

        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("bookstore.com")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli()
                ))
                .claim("userId", user.getId())
                .claim("role", getRoles(user))
                .jwtID(UUID.randomUUID().toString()) // Random 1 ID ngẫu nhiên cho Token để hỗ trợ Logout
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize(); // Trả về chuỗi dạng text (Header.Payload.Signature)
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
