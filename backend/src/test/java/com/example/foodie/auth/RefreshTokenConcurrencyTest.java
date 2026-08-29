package com.example.foodie.auth;

import com.example.foodie.support.AbstractMySqlConcurrencyTest;
import com.example.foodie.support.ConcurrencyTestFixtures;
import com.example.foodie.support.ConcurrentRace;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RefreshTokenRepository.revokeIfActive là một UPDATE ... WHERE revoked_at IS NULL nguyên tử
 * ở tầng database -- số dòng bị ảnh hưởng (0 hay 1) quyết định ai thắng khi hai request refresh
 * cùng một token đến thật sự đồng thời. Nếu race này không được chặn đúng, cả hai request có
 * thể cùng đọc revoked_at=NULL, cùng thành công, và cấp ra HAI refresh token mới hợp lệ từ một
 * token cha -- tạo hai nhánh phiên song song, đúng lỗ hổng mà RefreshTokenReuseDetectionTest
 * (Phase 5, luồng đơn) không chạm tới được.
 */
class RefreshTokenConcurrencyTest extends AbstractMySqlConcurrencyTest {

    @Autowired
    private ObjectMapper objectMapper;

    private ConcurrencyTestFixtures fixtures;

    @BeforeEach
    void setUp() {
        fixtures = new ConcurrencyTestFixtures(restTemplate, objectMapper);
    }

    @Test
    @Timeout(25)
    void should_letExactlyOneWin_when_sameRefreshTokenUsedConcurrently() throws Exception {
        ConcurrencyTestFixtures.RegisteredUser user = fixtures.registerUser();
        assertThat(user.refreshTokenCookie()).isNotBlank();

        List<Callable<HttpStatusCode>> tasks = List.of(
                () -> refresh(user.refreshTokenCookie()).getStatusCode(),
                () -> refresh(user.refreshTokenCookie()).getStatusCode()
        );

        List<HttpStatusCode> results = ConcurrentRace.run(tasks, 20);

        assertThat(results).filteredOn(HttpStatus.OK::equals).hasSize(1);
        assertThat(results).filteredOn(HttpStatus.UNAUTHORIZED::equals).hasSize(1);
    }

    private ResponseEntity<String> refresh(String refreshTokenCookie) {
        ResponseEntity<String> response = fixtures.postWithCookie(
                "/api/v1/users/refresh", ConcurrencyTestFixtures.COOKIE_NAME, refreshTokenCookie);

        if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            JsonNode json = fixtures.readTree(response.getBody());
            assertThat(json.get("error").asText()).isEqualTo("REFRESH_TOKEN_REUSED");
        }
        return response;
    }
}
