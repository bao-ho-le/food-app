package com.example.foodie.auth.service;

import com.example.foodie.auth.dto.GeneratedRefreshToken;
import com.example.foodie.auth.entity.RefreshToken;
import com.example.foodie.auth.repository.RefreshTokenRepository;
import com.example.foodie.auth.security.CustomUserDetails;
import com.example.foodie.common.exception.ErrorCode;
import com.example.foodie.common.exception.business_exception.IdentityException;
import com.example.foodie.auth.dto.request.AdminDTO;
import com.example.foodie.auth.dto.request.ResetPasswordDTO;
import com.example.foodie.auth.dto.request.UserDTO;
import com.example.foodie.auth.dto.request.UserLoginDTO;
import com.example.foodie.auth.dto.response.AdminResponseDTO;
import com.example.foodie.auth.dto.response.UserLoginResponseDTO;
import com.example.foodie.auth.dto.response.UserResponseDTO;
import com.example.foodie.auth.helper.AuthHelper;
import com.example.foodie.auth.mapper.AuthMapper;
import com.example.foodie.identity.user.entity.Role;
import com.example.foodie.identity.user.entity.User;
import com.example.foodie.identity.user.enums.RoleName;
import com.example.foodie.identity.user.repository.RoleRepository;
import com.example.foodie.identity.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    public static final String REFRESH_TOKEN_COOKIE = "refreshToken";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JWTService jwtService;
    private final AuthHelper authHelper;
    private final AuthMapper authMapper;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${api.prefix}")
    private String apiPrefix;

    @Value("${cookie.secure}")
    private boolean cookieSecure;

    @Override
    @Transactional
    public UserResponseDTO register(UserDTO userDTO, HttpServletResponse response) {
        authHelper.validateUserRequest(userDTO);

        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new IdentityException(ErrorCode.USER_EMAIL_ALREADY_EXISTS);
        } else if (userRepository.existsByPhoneNumber(userDTO.getPhoneNumber())) {
            throw new IdentityException(ErrorCode.USER_PHONE_ALREADY_EXISTS);
        }

        Role role = roleRepository.findByRoleName(RoleName.USER)
                .orElseThrow(() -> new IdentityException(ErrorCode.USER_ROLE_NOT_FOUND));

        String encodedPassword = encoder.encode(userDTO.getPassword());
        userDTO.setPassword(encodedPassword);

        User savedUser = userRepository.save(authMapper.toEntity(userDTO, role));

        String accessToken = jwtService.generateAccessToken(savedUser.getId());
        GeneratedRefreshToken refreshToken = jwtService.generateRefreshToken(savedUser.getId());
        saveRefreshToken(savedUser, refreshToken);
        setRefreshTokenCookie(response, refreshToken);

        return authMapper.toRegisterResponse(userDTO, accessToken);
    }

    @Override
    @Transactional
    public AdminResponseDTO registerAdmin(AdminDTO adminDTO, HttpServletResponse response) {
        User savedUser = createAdminUser(adminDTO);

        String accessToken = jwtService.generateAccessToken(savedUser.getId());
        GeneratedRefreshToken refreshToken = jwtService.generateRefreshToken(savedUser.getId());
        saveRefreshToken(savedUser, refreshToken);
        setRefreshTokenCookie(response, refreshToken);

        return authMapper.toAdminRegisterResponse(adminDTO, accessToken);
    }

    @Override
    @Transactional
    public User createAdminUser(AdminDTO adminDTO) {
        authHelper.validateUserRequest(adminDTO);

        if (userRepository.existsByEmail(adminDTO.getEmail())) {
            throw new IdentityException(ErrorCode.USER_EMAIL_ALREADY_EXISTS);
        } else if (userRepository.existsByPhoneNumber(adminDTO.getPhoneNumber())) {
            throw new IdentityException(ErrorCode.USER_PHONE_ALREADY_EXISTS);
        }

        Role role = roleRepository.findByRoleName(RoleName.ADMIN)
                .orElseThrow(() -> new IdentityException(ErrorCode.USER_ROLE_NOT_FOUND));

        String encodedPassword = encoder.encode(adminDTO.getPassword());
        adminDTO.setPassword(encodedPassword);

        return userRepository.save(authMapper.toEntity(adminDTO, role));
    }

    @Override
    @Transactional
    public UserLoginResponseDTO login(UserLoginDTO userLoginDTO, HttpServletResponse response) {
        authHelper.validateLoginRequest(userLoginDTO);

        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(userLoginDTO.getEmail(), userLoginDTO.getPassword()));

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        String accessToken = jwtService.generateAccessToken(user.getId());
        GeneratedRefreshToken refreshToken = jwtService.generateRefreshToken(user.getId());
        saveRefreshToken(user, refreshToken);
        setRefreshTokenCookie(response, refreshToken);

        return authMapper.toLoginResponse(user, userLoginDTO.getEmail(), accessToken);
    }

    @Override
    @Transactional
    public void resetPassword(String email, ResetPasswordDTO resetPasswordDTO) {
        authHelper.validateResetPasswordRequest(resetPasswordDTO);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IdentityException(ErrorCode.USER_NOT_FOUND));

        if (encoder.matches(resetPasswordDTO.getOldPassword(), user.getPassword())) {
            String encodedPassword = encoder.encode(resetPasswordDTO.getNewPassword());
            user.setPassword(encodedPassword);
            userRepository.save(user);

            refreshTokenRepository.revokeAllByUser(user, Instant.now());
        } else {
            throw new IdentityException(ErrorCode.USER_OLD_PASSWORD_INCORRECT);
        }
    }

    @Override
    @Transactional
    public UserLoginResponseDTO refresh(String refreshTokenStr, HttpServletResponse response) {

        Claims claims;
        try {
            claims = jwtService.parseRefreshToken(refreshTokenStr);
        } catch (JwtException | IllegalArgumentException e) {
            throw new IdentityException(ErrorCode.TOKEN_INVALID);
        }

        String jti = claims.getId();
        Integer userId = jwtService.extractUserId(claims);

        Instant now = Instant.now();
        int updated = refreshTokenRepository.revokeIfActive(jti, now);

        // Xử lí trường hợp token đã bị revoke rồi, nhưng lại gửi request với token cũ
        // tức là đang có khả năng token bị đánh cấp -> revoke all token
        if (updated == 0) {
            refreshTokenRepository.revokeAllByUser(userRepository.getReferenceById(userId), now);
            log.warn("Refresh token reuse detected for userId={}", userId);
            throw new IdentityException(ErrorCode.REFRESH_TOKEN_REUSED);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IdentityException(ErrorCode.USER_NOT_FOUND));
        if (!user.isActive()) {
            throw new IdentityException(ErrorCode.ACCOUNT_DISABLED);
        }

        String newAccessToken = jwtService.generateAccessToken(userId);
        GeneratedRefreshToken newRefresh = jwtService.generateRefreshToken(userId);
        saveRefreshToken(user, newRefresh);
        setRefreshTokenCookie(response, newRefresh);

        return authMapper.toRefreshResponse(user, newAccessToken);
    }

    @Override
    @Transactional
    public void logout(String refreshTokenStr, HttpServletResponse response) {
        try {
            if (refreshTokenStr != null) {
                Claims claims = jwtService.parseRefreshToken(refreshTokenStr);
                String jti = claims.getId();
                refreshTokenRepository.revokeIfActive(jti, Instant.now());
            }
        } catch (JwtException | IllegalArgumentException e) {
            // Idempotent — token rác/hết hạn/đã revoke đều coi là "đã logout", không throw
        } finally {
            clearRefreshTokenCookie(response);
        }
    }


    // Helper

    private void saveRefreshToken(User user, GeneratedRefreshToken refreshToken) {
        RefreshToken entity = RefreshToken.builder()
                .jti(refreshToken.jti())
                .user(user)
                .expiresAt(refreshToken.expiresAt())
                .build();
        refreshTokenRepository.save(entity);
    }

    private void setRefreshTokenCookie(HttpServletResponse response, GeneratedRefreshToken refreshToken) {
        Duration maxAge = Duration.between(Instant.now(), refreshToken.expiresAt());
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, refreshToken.token())
                .httpOnly(true)
                .secure(cookieSecure)
                .path(apiPrefix + "/users")
                .sameSite(cookieSecure ? "None" : "Lax")
                .maxAge(maxAge)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path(apiPrefix + "/users")
                .sameSite(cookieSecure ? "None" : "Lax")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
