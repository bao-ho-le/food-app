package com.example.foodie.auth;

import com.example.foodie.identity.user.repository.UserRepository;
import com.example.foodie.support.AbstractMySqlIntegrationTest;
import com.example.foodie.support.SystemTestFixtures;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockCookie;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Vòng đời phiên đăng nhập, phần còn lại sau RefreshTokenReuseDetectionTest (không lặp lại
 * việc phát hiện reuse token ở đó). Item 1 và 2 đặc biệt quan trọng: chúng chứng minh việc
 * thu hồi/khoá THỰC SỰ COMMIT xuống DB -- đúng dạng bug đã từng ẩn nấp trong dự án này (lệnh
 * thu hồi chạy nhưng bị @Transactional rollback ngay sau đó, và test tầng mock vẫn xanh vì
 * Mockito không có khái niệm transaction).
 */
class TokenLifecycleTest extends AbstractMySqlIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private SystemTestFixtures fixtures;

    @BeforeEach
    void setUp() {
        fixtures = new SystemTestFixtures(mockMvc, objectMapper);
    }

    // ---- item 1: đổi mật khẩu phải chấm dứt MỌI phiên cũ ----

    @Test
    void should_invalidateOldRefreshToken_when_passwordChanged() throws Exception {
        SystemTestFixtures.RegisteredUser user = fixtures.registerUser();

        Map<String, Object> resetBody = Map.of(
                "oldPassword", "SuperSecret123",
                "newPassword", "BrandNewSecret456");
        mockMvc.perform(put("/api/v1/users/password")
                        .header("Authorization", SystemTestFixtures.bearer(user.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetBody)))
                .andExpect(status().isOk());

        // refreshTokenRepository.revokeAllByUser(...) chạy trong resetPassword() -- nếu bị
        // rollback (bug từng có), request này sẽ vẫn trả 200 thay vì 401.
        mockMvc.perform(post("/api/v1/users/refresh")
                        .cookie(new MockCookie(SystemTestFixtures.COOKIE_NAME, user.refreshTokenCookie())))
                .andExpect(status().isUnauthorized());
    }

    // ---- item 2: admin khoá tài khoản -- access token còn hạn vẫn bị chặn ngay ----

    @Test
    void should_rejectImmediately_when_accountDisabledWhileAccessTokenStillValid() throws Exception {
        SystemTestFixtures.RegisteredUser user = fixtures.registerUser();
        Integer userId = userRepository.findByEmail(user.email()).orElseThrow().getId();

        String adminToken = fixtures.adminAccessToken();
        mockMvc.perform(post("/api/v1/admin/users/blocking/" + userId + "/0")
                        .header("Authorization", SystemTestFixtures.bearer(adminToken)))
                .andExpect(status().isOk());

        // Access token chưa hết hạn, chưa hề bị thu hồi -- JWTFilter phải tra lại
        // isActive từ DB trên MỖI request (không chỉ tin vào chữ ký/hạn của token).
        mockMvc.perform(get("/api/v1/orders/user")
                        .header("Authorization", SystemTestFixtures.bearer(user.accessToken())))
                .andExpect(status().isUnauthorized());
    }

    // ---- item 3: logout luôn idempotent dù cookie thế nào ----

    @Test
    void should_return204_when_logoutRegardlessOfCookieState() throws Exception {
        mockMvc.perform(post("/api/v1/users/logout"))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/v1/users/logout")
                        .cookie(new MockCookie(SystemTestFixtures.COOKIE_NAME, "hoan-toan-rac")))
                .andExpect(status().isNoContent());

        SystemTestFixtures.RegisteredUser user = fixtures.registerUser();
        mockMvc.perform(post("/api/v1/users/logout")
                        .cookie(new MockCookie(SystemTestFixtures.COOKIE_NAME, user.refreshTokenCookie())))
                .andExpect(status().isNoContent());
    }

    // ---- item 4: thuộc tính cookie sau khi đăng ký ----
    // Hoãn từ Phase 3 sang đây vì cookie do AuthServiceImpl set qua HttpServletResponse --
    // Phase 3 mock service nên không cookie nào tồn tại để kiểm.

    @Test
    void should_setCookieWithCorrectAttributes_when_registering() throws Exception {
        Map<String, Object> body = Map.of(
                "fullName", "Cookie Tester",
                "gender", "MALE",
                "phoneNumber", SystemTestFixtures.randomVietnamesePhone(),
                "email", "cookie-" + System.nanoTime() + "@test.local",
                "password", "SuperSecret123"
        );

        MvcResult result = mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn();

        String setCookie = result.getResponse().getHeader("Set-Cookie");
        assertThat(setCookie).isNotNull();
        assertThat(setCookie).contains("HttpOnly");
        assertThat(setCookie).contains("SameSite=Lax");
        assertThat(setCookie).contains("Path=/api/v1/users");
    }

    // ---- item 5: cookie bị xoá sau logout ----

    @Test
    void should_setMaxAgeZero_when_loggingOut() throws Exception {
        SystemTestFixtures.RegisteredUser user = fixtures.registerUser();

        MvcResult result = mockMvc.perform(post("/api/v1/users/logout")
                        .cookie(new MockCookie(SystemTestFixtures.COOKIE_NAME, user.refreshTokenCookie())))
                .andExpect(status().isNoContent())
                .andReturn();

        String setCookie = result.getResponse().getHeader("Set-Cookie");
        assertThat(setCookie).isNotNull();
        assertThat(setCookie).contains("Max-Age=0");
    }
}
