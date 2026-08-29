package com.example.foodie.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * BUG-001 (xem foodie-audit report): AuthServiceImpl.refresh() thu hồi toàn bộ
 * refresh-token family khi phát hiện reuse, NHƯNG method này là @Transactional
 * và lệnh thu hồi đó xảy ra ngay trước một `throw` — nên Spring rollback chính
 * lệnh thu hồi đó. INV-11 (report, mục C): "phát hiện reuse ⇒ toàn bộ family
 * bị thu hồi ngay" — kỳ vọng đây phải đúng ở mọi thời điểm.
 *
 * Test này đi qua toàn bộ tầng thật (controller → security filter → service →
 * transaction → H2) — không mock gì. Đây là loại hành vi mà Mockito không thể
 * chứng minh: nó phụ thuộc việc transaction có thực sự commit hay rollback.
 *
 * Đặt tên *Test (không phải *IT) vì repo này chỉ có maven-surefire-plugin —
 * không có failsafe wiring, nên hậu tố *IT sẽ không bao giờ được `mvn test`
 * chạy tới.
 */
@SpringBootTest
@AutoConfigureMockMvc
class RefreshTokenReuseDetectionTest {

    private static final String USERS = "/api/v1/users";
    private static final String COOKIE_NAME = "refreshToken";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void reuseOfRevokedRefreshToken_mustInvalidateTheRotatedTokenToo() throws Exception {
        // --- Đăng ký user mới, nhận refresh token đầu tiên (RT1) qua cookie ---
        String rt1 = registerAndGetRefreshTokenCookie();
        assertThat(rt1).isNotBlank();

        // --- Dùng RT1 để refresh hợp lệ ⇒ được cấp RT2, RT1 bị revoke ---
        MvcResult firstRefresh = mockMvc.perform(post(USERS + "/refresh")
                        .cookie(refreshCookie(rt1)))
                .andExpect(status().isOk())
                .andReturn();
        String rt2 = extractRefreshTokenCookie(firstRefresh.getResponse());
        assertThat(rt2).as("refresh hợp lệ phải cấp refresh token mới (rotation)").isNotBlank();
        assertThat(rt2).isNotEqualTo(rt1);

        // --- Attacker replay RT1 (đã bị revoke ở bước trên) ⇒ phải bị từ chối ---
        mockMvc.perform(post(USERS + "/refresh").cookie(refreshCookie(rt1)))
                .andExpect(status().isUnauthorized());

        // --- INV-11: việc phát hiện reuse phải thu hồi CẢ RT2 (token hợp lệ đang
        // hoạt động), vì không thể biết ai giữ RT1 — nạn nhân hay kẻ tấn công.
        // Đây là điểm mà BUG-001 lộ ra: do @Transactional rollback, RT2 KHÔNG
        // hề bị thu hồi, nên request dưới đây hiện tại vẫn trả 200 thay vì 401. ---
        mockMvc.perform(post(USERS + "/refresh").cookie(refreshCookie(rt2)))
                .andExpect(status().isUnauthorized());
    }

    // ---- helpers ----

    private String registerAndGetRefreshTokenCookie() throws Exception {
        Map<String, Object> body = Map.of(
                "fullName", "Reuse Detection Tester",
                "gender", "MALE",
                "phoneNumber", randomVietnamesePhone(),
                "email", "reuse-" + System.nanoTime() + "@test.local",
                "password", "SuperSecret123"
        );

        MvcResult result = mockMvc.perform(post(USERS + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn();

        String rt = extractRefreshTokenCookie(result.getResponse());
        assertThat(rt).as("register phải set cookie refreshToken").isNotBlank();
        return rt;
    }

    private static String extractRefreshTokenCookie(MockHttpServletResponse response) {
        var cookie = response.getCookie(COOKIE_NAME);
        return cookie != null ? cookie.getValue() : null;
    }

    private static org.springframework.mock.web.MockCookie refreshCookie(String value) {
        return new org.springframework.mock.web.MockCookie(COOKIE_NAME, value);
    }

    private static String randomVietnamesePhone() {
        // "0" + 9 chữ số ngẫu nhiên, khớp regex ^(0|\+84)(\d{9})$
        long n = ThreadLocalRandom.current().nextLong(100_000_000L, 999_999_999L);
        return "0" + n;
    }
}
