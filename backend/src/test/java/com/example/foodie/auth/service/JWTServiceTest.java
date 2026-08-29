package com.example.foodie.auth.service;

import com.example.foodie.auth.config.JwtProperties;
import com.example.foodie.auth.dto.GeneratedRefreshToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

// EP theo vòng đời và loại token (access/refresh), không có Spring context —
// JwtProperties được new + set tay bằng secret Base64 hợp lệ mượn từ
// src/test/resources/application.properties.
class JWTServiceTest {

    // Cùng secret với src/test/resources/application.properties để không phải phát minh secret mới.
    private static final String ACCESS_SECRET = "dGVzdC1vbmx5LXNlY3JldC1kby1ub3QtdXNlLWluLXByb2Q9MTIzNDU2Nzg5MA==";
    private static final String REFRESH_SECRET = "dGVzdC1vbmx5LXJlZnJlc2gtc2VjcmV0LWRvLW5vdC11c2U9MTIzNDU2Nzg5MA==";
    private static final long ACCESS_EXPIRATION = 3_600_000L;
    private static final long REFRESH_EXPIRATION = 604_800_000L;

    private final JWTService jwtService = new JWTService(buildProperties());

    private static JwtProperties buildProperties() {
        JwtProperties.Token access = new JwtProperties.Token();
        access.setSecret(ACCESS_SECRET);
        access.setExpiration(ACCESS_EXPIRATION);

        JwtProperties.Token refresh = new JwtProperties.Token();
        refresh.setSecret(REFRESH_SECRET);
        refresh.setExpiration(REFRESH_EXPIRATION);

        JwtProperties properties = new JwtProperties();
        properties.setAccessToken(access);
        properties.setRefreshToken(refresh);
        return properties;
    }

    // #1: access token hợp lệ phải parse được và mang đúng claim nghiệp vụ.
    @Test
    @DisplayName("parseAccessToken trả về claims đúng userId, type=ACCESS, issuer=foodie cho token hợp lệ")
    void should_parseValidAccessToken_when_tokenIsAccessTypeAndUnexpired() {
        String token = jwtService.generateAccessToken(42);

        Claims claims = jwtService.parseAccessToken(token);

        assertThat(jwtService.extractUserId(claims)).isEqualTo(42);
        assertThat(claims.get("type", String.class)).isEqualTo("ACCESS");
        assertThat(claims.getIssuer()).isEqualTo("foodie");
    }

    // #2 - P0: chặn token type confusion. Refresh token (vòng đời dài, lưu cookie)
    // không được phép dùng thay access token, nếu không toàn bộ mô hình xoay vòng
    // token mất tác dụng.
    @Test
    @DisplayName("parseAccessToken ném JwtException khi nhận vào một refresh token")
    void should_throwJwtException_when_parseAccessTokenReceivesRefreshToken() {
        GeneratedRefreshToken refreshToken = jwtService.generateRefreshToken(42);

        assertThatThrownBy(() -> jwtService.parseAccessToken(refreshToken.token()))
                .isInstanceOf(JwtException.class);
    }

    // #3 - P0: chiều ngược lại của #2.
    @Test
    @DisplayName("parseRefreshToken ném JwtException khi nhận vào một access token")
    void should_throwJwtException_when_parseRefreshTokenReceivesAccessToken() {
        String accessToken = jwtService.generateAccessToken(42);

        assertThatThrownBy(() -> jwtService.parseRefreshToken(accessToken))
                .isInstanceOf(JwtException.class);
    }

    // #4: token ký bằng secret không khớp secret cấu hình phải bị từ chối ở bước
    // verify chữ ký, trước khi tới bước kiểm claim "type".
    @Test
    @DisplayName("parseAccessToken ném exception khi token ký bằng secret khác")
    void should_throwJwtException_when_accessTokenSignedWithWrongSecret() {
        String wrongSecret = Base64.getEncoder().encodeToString(
                "this-is-a-completely-different-secret-key-not-configured".getBytes());
        SecretKey wrongKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(wrongSecret));
        String token = Jwts.builder()
                .subject("42")
                .claim("type", "ACCESS")
                .issuer("foodie")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ACCESS_EXPIRATION))
                .signWith(wrongKey)
                .compact();

        assertThatThrownBy(() -> jwtService.parseAccessToken(token))
                .isInstanceOf(JwtException.class);
    }

    // #5: JWT "alg: none" (không chữ ký) tự chế bằng tay, không đi qua Jwts.builder(),
    // để chắc chắn test không phụ thuộc vào việc builder có hỗ trợ unsecured JWT hay không.
    @Test
    @DisplayName("parseAccessToken ném exception khi token có alg=none, không được parse thành công")
    void should_throwJwtException_when_tokenHasNoneAlgorithm() {
        String header = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"none\"}".getBytes());
        String payload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"sub\":\"42\",\"type\":\"ACCESS\"}".getBytes());
        String noneToken = header + "." + payload + ".";

        assertThatThrownBy(() -> jwtService.parseAccessToken(noneToken))
                .isInstanceOf(JwtException.class);
    }

    // #6: token đã hết hạn phải ném đúng loại con ExpiredJwtException, không phải
    // JwtException chung chung — frontend cần phân biệt "hết hạn" với "không hợp lệ".
    @Test
    @DisplayName("parseAccessToken ném ExpiredJwtException khi token đã hết hạn")
    void should_throwExpiredJwtException_when_accessTokenExpired() {
        SecretKey accessKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(ACCESS_SECRET));
        Date past = new Date(System.currentTimeMillis() - ACCESS_EXPIRATION);
        String expiredToken = Jwts.builder()
                .subject("42")
                .claim("type", "ACCESS")
                .issuer("foodie")
                .issuedAt(new Date(past.getTime() - 1000))
                .expiration(past)
                .signWith(accessKey)
                .compact();

        assertThatThrownBy(() -> jwtService.parseAccessToken(expiredToken))
                .isInstanceOf(ExpiredJwtException.class);
    }

    // #7: chuỗi rác không phải JWT phải ném JwtException (lỗi định dạng), tuyệt đối
    // không được để lọt NullPointerException ra ngoài — đó là dấu hiệu thiếu kiểm tra đầu vào.
    @Test
    @DisplayName("parseAccessToken ném JwtException (không phải NullPointerException) với chuỗi rác")
    void should_throwJwtExceptionNotNullPointer_when_tokenIsGarbageString() {
        assertThatThrownBy(() -> jwtService.parseAccessToken("khong-phai-jwt-hop-le"))
                .isInstanceOf(JwtException.class)
                .isNotInstanceOf(NullPointerException.class);
    }

    // #8: hai lần generate liên tiếp phải cho jti khác nhau (định danh duy nhất để
    // revoke từng refresh token riêng lẻ) và expiresAt phải khớp now + expiration.
    @Test
    @DisplayName("generateRefreshToken gọi 2 lần cho 2 jti khác nhau, expiresAt đúng now + expiration")
    void should_generateDistinctJtiAndCorrectExpiry_when_generateRefreshTokenCalledTwice() {
        Instant before = Instant.now();
        GeneratedRefreshToken first = jwtService.generateRefreshToken(42);
        GeneratedRefreshToken second = jwtService.generateRefreshToken(42);

        assertThat(first.jti()).isNotEqualTo(second.jti());
        assertThat(first.expiresAt())
                .isCloseTo(before.plusMillis(REFRESH_EXPIRATION), within(5, ChronoUnit.SECONDS));
    }

    // #9: subject của claim phải parse đúng ngược lại thành userId gốc.
    @Test
    @DisplayName("extractUserId parse đúng subject thành Integer")
    void should_extractOriginalUserId_when_claimsHaveIntegerSubject() {
        Claims claims = jwtService.parseAccessToken(jwtService.generateAccessToken(777));

        assertThat(jwtService.extractUserId(claims)).isEqualTo(777);
    }
}
