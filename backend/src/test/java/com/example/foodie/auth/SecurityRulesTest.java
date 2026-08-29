package com.example.foodie.auth;

import com.example.foodie.support.AbstractMySqlIntegrationTest;
import com.example.foodie.support.SystemTestFixtures;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Phase 3 tắt hẳn filter chain (addFilters = false) để cô lập tầng MVC -- phân quyền thật
 * hoàn toàn CHƯA được kiểm ở đâu trước Phase 5. Class này chạy trên filter chain thật
 * (JWTFilter + SecurityConfig thật), là nơi duy nhất chứng minh ma trận endpoint × vai trò
 * trong SecurityConfig hoạt động đúng như khai báo.
 */
class SecurityRulesTest extends AbstractMySqlIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private SystemTestFixtures fixtures;

    @BeforeEach
    void setUp() {
        fixtures = new SystemTestFixtures(mockMvc, objectMapper);
    }

    // ---- item 1, 2: endpoint công khai không cần token ----

    @Test
    void should_return200_when_anonymousCallsPublicTagsEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/tags")).andExpect(status().isOk());
    }

    @Test
    void should_return200_when_anonymousCallsPublicDishesEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/dishes")).andExpect(status().isOk());
    }

    // ---- item 3: /users/login phải gọi được (permitAll), dù thân request rỗng ----
    // Nếu bị chặn bởi security filter chain, mọi request tới đây đều nhận 401
    // AUTHENTICATION_REQUIRED bất kể body -- 400 VALIDATION_FAILED chỉ có thể xảy ra nếu
    // request đã lọt qua filter chain và chạm tới @Valid trên controller.

    @Test
    void should_notReturn401_when_anonymousCallsLoginEndpoint() throws Exception {
        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));
    }

    // ---- item 4: endpoint cần đăng nhập, gọi ẩn danh ----

    @Test
    void should_return401AuthenticationRequired_when_anonymousCallsOwnOrders() throws Exception {
        mockMvc.perform(get("/api/v1/orders/user"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("AUTHENTICATION_REQUIRED"));
    }

    // ---- item 5, 6: endpoint chỉ ADMIN ----

    @Test
    void should_return403AccessDenied_when_userCallsAdminOrders() throws Exception {
        SystemTestFixtures.RegisteredUser user = fixtures.registerUser();

        mockMvc.perform(get("/api/v1/admin/orders")
                        .header("Authorization", SystemTestFixtures.bearer(user.accessToken())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ACCESS_DENIED"));
    }

    @Test
    void should_notReturn403_when_adminCallsAdminOrders() throws Exception {
        String adminToken = fixtures.adminAccessToken();

        mockMvc.perform(get("/api/v1/admin/orders")
                        .header("Authorization", SystemTestFixtures.bearer(adminToken)))
                .andExpect(status().isOk());
    }

    // ---- item 7: chống leo thang đặc quyền -- USER không tự tạo được tài khoản ADMIN ----

    @Test
    void should_return403_when_userTriesToRegisterAdmin() throws Exception {
        SystemTestFixtures.RegisteredUser user = fixtures.registerUser();

        java.util.Map<String, Object> body = java.util.Map.of(
                "fullName", "Fake Admin",
                "gender", "MALE",
                "phoneNumber", SystemTestFixtures.randomVietnamesePhone(),
                "email", "fake-admin-" + System.nanoTime() + "@test.local",
                "password", "SuperSecret123"
        );

        mockMvc.perform(post("/api/v1/admin/users/register-admin")
                        .header("Authorization", SystemTestFixtures.bearer(user.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ACCESS_DENIED"));
    }

    // ---- item 8: endpoint ADMIN khác ----

    @Test
    void should_return403_when_userUploadsImage() throws Exception {
        SystemTestFixtures.RegisteredUser user = fixtures.registerUser();

        mockMvc.perform(post("/api/v1/images")
                        .header("Authorization", SystemTestFixtures.bearer(user.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ACCESS_DENIED"));
    }

    // ---- item 9: header Authorization dạng khác (Basic) -- không được crash 500 ----

    @Test
    void should_return401NotFail500_when_authorizationHeaderIsBasicScheme() throws Exception {
        mockMvc.perform(get("/api/v1/orders/user")
                        .header("Authorization", "Basic xyz"))
                .andExpect(status().isUnauthorized());
    }

    // ---- item 10: JWT hợp lệ về hình thức nhưng ký bằng khoá khác ----
    // JWTFilter bắt JwtException từ việc xác minh chữ ký, coi như không có token -- request
    // rơi về ẩn danh và bị anyRequest().authenticated() chặn. Nếu ai đó lỡ đổi verifyWith()
    // thành parse không kiểm chữ ký, test này đỏ ngay.

    @Test
    void should_return401_when_jwtSignedWithWrongKey() throws Exception {
        SecretKey wrongKey = Keys.hmacShaKeyFor(new byte[32]);
        Date now = new Date();
        String forgedToken = Jwts.builder()
                .subject("1")
                .claim("jti", UUID.randomUUID().toString())
                .claim("type", "ACCESS")
                .issuer("foodie")
                .issuedAt(now)
                .expiration(new Date(now.getTime() + 3_600_000))
                .signWith(wrongKey)
                .compact();

        mockMvc.perform(get("/api/v1/orders/user")
                        .header("Authorization", SystemTestFixtures.bearer(forgedToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("AUTHENTICATION_REQUIRED"));
    }
}
