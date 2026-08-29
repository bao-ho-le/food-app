package com.example.foodie.ordering.order;

import com.example.foodie.support.AbstractMySqlConcurrencyTest;
import com.example.foodie.support.ConcurrencyTestFixtures;
import com.example.foodie.support.ConcurrentRace;
import com.example.foodie.support.SystemTestFixtures;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ⚠️ THÍ NGHIỆM XÁC NHẬN, không phải test hồi quy thường.
 *
 * Đặc tả: hai request POST /orders đồng thời cùng Idempotency-Key phải cho đúng MỘT thao tác
 * nghiệp vụ -- request thắng nhận 201, request thua nhận 409 REQUEST_IN_PROGRESS (một phản hồi
 * "có ý nghĩa", không phải lỗi hệ thống).
 *
 * Dự đoán (suy luận từ đọc code, CHƯA xác nhận): IdempotencyKeyStore.tryReserve bắt
 * DataIntegrityViolationException BÊN TRONG chính transaction REQUIRES_NEW của nó rồi return
 * Optional.empty() bình thường. Nếu flush thất bại (đâm UNIQUE PK trên idempotency_key) đã đủ
 * để Hibernate đánh dấu transaction đó rollback-only ở tầng vật lý, Spring sẽ ném
 * UnexpectedRollbackException ngay khi @Transactional advice cố commit một transaction đã bị
 * đánh dấu rollback-only -- request thua nhận 500 thay vì 409.
 *
 * KHÔNG được sửa expected thành 500 dù quan sát thấy 500. Assertion dưới đây giữ nguyên đặc tả
 * (409 REQUEST_IN_PROGRESS); nếu quan sát khác đặc tả, test PHẢI đỏ và kết quả được ghi vào báo
 * cáo cuối phiên (mã lỗi thực tế, có tái hiện ổn định không, trạng thái database sau khi chạy).
 */
class IdempotencyConcurrencyTest extends AbstractMySqlConcurrencyTest {

    @Autowired
    private ObjectMapper objectMapper;

    private ConcurrencyTestFixtures fixtures;
    private String adminToken;
    private Integer restaurantId;

    @BeforeEach
    void setUp() {
        fixtures = new ConcurrencyTestFixtures(restTemplate, objectMapper);
        adminToken = fixtures.adminAccessToken();
        restaurantId = fixtures.createRestaurant(adminToken);
    }

    @AfterEach
    void tearDown() {
        SystemTestFixtures.resetOrderingData(jdbcTemplate);
    }

    // ---- item 1: hai request đồng thời cùng key -- thí nghiệm xác nhận dự đoán 500 ----

    @Test
    @Timeout(25)
    void should_letExactlyOneSucceed_when_sameIdempotencyKeyUsedConcurrently() throws Exception {
        Integer dishId = fixtures.createDish(adminToken, restaurantId, 10);
        ConcurrencyTestFixtures.RegisteredUser user = fixtures.registerUser();
        Integer addressId = fixtures.createAddress(user.accessToken(), "Idempotency Race Street", false);
        fixtures.addToCart(user.accessToken(), dishId, 2);

        String idempotencyKey = UUID.randomUUID().toString();
        Map<String, Object> body = Map.of("addressId", addressId);

        List<Callable<ResponseEntity<String>>> tasks = List.of(
                () -> fixtures.postWithIdempotencyKey("/api/v1/orders", body, user.accessToken(), idempotencyKey),
                () -> fixtures.postWithIdempotencyKey("/api/v1/orders", body, user.accessToken(), idempotencyKey)
        );

        List<ResponseEntity<String>> results = ConcurrentRace.run(tasks, 20);

        List<HttpStatusCode> statuses = results.stream().map(ResponseEntity::getStatusCode).toList();
        long created = statuses.stream().filter(HttpStatus.CREATED::equals).count();
        long conflict = statuses.stream().filter(HttpStatus.CONFLICT::equals).count();

        assertThat(created).as("đúng 1 request phải tạo đơn thành công").isEqualTo(1);

        // Bất biến quan trọng NHẤT của cả test này: sai mã lỗi thì khó chịu, nhưng tạo hai đơn
        // thì mất tiền thật. Giữ assertion này bất kể mã lỗi ở trên đúng hay sai.
        Integer orderCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM orders WHERE user_id = (SELECT id FROM user WHERE email = ?)",
                Integer.class, user.email());
        assertThat(orderCount).as("chỉ đúng 1 đơn được tạo trong database dù mã lỗi phía trên là gì").isEqualTo(1);
        assertThat(fixtures.readDishStock(dishId)).as("kho chỉ bị trừ đúng 1 lần").isEqualTo(8);

        // Đặc tả: request thua phải nhận 409 REQUEST_IN_PROGRESS. Đây là phần được giữ nguyên
        // dù dự đoán là quan sát thực tế có thể là 500 -- không nới lỏng theo hành vi quan sát.
        assertThat(conflict).as("đúng 1 request phải nhận 409 REQUEST_IN_PROGRESS theo đặc tả").isEqualTo(1);
        String losingError = results.stream()
                .filter(r -> r.getStatusCode() != HttpStatus.CREATED)
                .findFirst()
                .map(r -> fixtures.readTree(r.getBody()).get("error").asText())
                .orElseThrow();
        assertThat(losingError).isEqualTo("REQUEST_IN_PROGRESS");
    }

    // ---- item 2: request thứ ba, sau khi cả hai request trên đã xong -- phải phát lại đúng ----

    @Test
    @Timeout(25)
    void should_replayStoredResponse_when_sameKeyRetriedAfterBothInFlightRequestsSettled() throws Exception {
        Integer dishId = fixtures.createDish(adminToken, restaurantId, 10);
        ConcurrencyTestFixtures.RegisteredUser user = fixtures.registerUser();
        Integer addressId = fixtures.createAddress(user.accessToken(), "Idempotency Replay Street", false);
        fixtures.addToCart(user.accessToken(), dishId, 1);

        String idempotencyKey = UUID.randomUUID().toString();
        Map<String, Object> body = Map.of("addressId", addressId);

        List<Callable<ResponseEntity<String>>> tasks = List.of(
                () -> fixtures.postWithIdempotencyKey("/api/v1/orders", body, user.accessToken(), idempotencyKey),
                () -> fixtures.postWithIdempotencyKey("/api/v1/orders", body, user.accessToken(), idempotencyKey)
        );
        List<ResponseEntity<String>> firstRound = ConcurrentRace.run(tasks, 20);

        ResponseEntity<String> successResponse = firstRound.stream()
                .filter(r -> r.getStatusCode() == HttpStatus.CREATED)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Không có request nào thành công ở lượt đầu, không thể kiểm replay"));
        JsonNode successBody = fixtures.readTree(successResponse.getBody());

        ResponseEntity<String> replay = fixtures.postWithIdempotencyKey(
                "/api/v1/orders", body, user.accessToken(), idempotencyKey);

        assertThat(replay.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(fixtures.readTree(replay.getBody()).get("id").asInt())
                .as("phải phát lại đúng response đã lưu, không tạo đơn mới")
                .isEqualTo(successBody.get("id").asInt());

        Integer orderCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM orders WHERE user_id = (SELECT id FROM user WHERE email = ?)",
                Integer.class, user.email());
        assertThat(orderCount).isEqualTo(1);
    }
}
