package com.example.foodie.ordering.order;

import com.example.foodie.support.AbstractMySqlIntegrationTest;
import com.example.foodie.support.SystemTestFixtures;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * IdempotencyKeyStore/IdempotencyService dùng @Transactional(REQUIRES_NEW) qua ba giai đoạn
 * (reserve/thực thi/commit kết quả) -- chỉ quan sát được đúng qua ba request HTTP rời rạc với
 * transaction commit thật, Mockito (Phase 2) không chạm tới được.
 */
class OrderIdempotencyTest extends AbstractMySqlIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private SystemTestFixtures fixtures;
    private String adminToken;
    private Integer restaurantId;

    @BeforeEach
    void setUp() throws Exception {
        fixtures = new SystemTestFixtures(mockMvc, objectMapper);
        adminToken = fixtures.adminAccessToken();
        restaurantId = fixtures.createRestaurant(adminToken);
    }

    // Xem OrderStockAccountingTest -- Phase 5 không rollback nên phải tự dọn order/dish/
    // restaurant để không làm sai các query đếm/tổng không lọc phạm vi ở Phase 4.
    @AfterEach
    void tearDown() {
        SystemTestFixtures.resetOrderingData(jdbcTemplate);
    }

    // ---- item 1: gọi lại cùng key + cùng body -> phát lại đúng kết quả cũ ----

    @Test
    void should_replaySameResponse_when_sameKeyAndSameBodyCalledTwice() throws Exception {
        Integer dishId = fixtures.createDish(adminToken, restaurantId, 10);
        SystemTestFixtures.RegisteredUser user = fixtures.registerUser();
        Integer addressId = fixtures.createAddress(user.accessToken(), "Idempotent Street");
        fixtures.addToCart(user.accessToken(), dishId, 2);

        String idempotencyKey = "idem-" + System.nanoTime();
        String body = objectMapper.writeValueAsString(Map.of("addressId", addressId));

        MvcResult first = mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", SystemTestFixtures.bearer(user.accessToken()))
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        MvcResult second = mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", SystemTestFixtures.bearer(user.accessToken()))
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn();

        assertThat(second.getResponse().getStatus()).isEqualTo(first.getResponse().getStatus());
        assertThat(second.getResponse().getContentAsString()).isEqualTo(first.getResponse().getContentAsString());

        // Chỉ 1 đơn thật sự được tạo, tồn kho chỉ bị trừ một lần (10 - 2, không phải 10 - 4).
        MvcResult ordersResult = mockMvc.perform(get("/api/v1/orders/user")
                        .header("Authorization", SystemTestFixtures.bearer(user.accessToken())))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode orders = objectMapper.readTree(ordersResult.getResponse().getContentAsString());
        assertThat(orders).hasSize(1);
        assertThat(fixtures.readDishStock(dishId)).isEqualTo(8);
    }

    // ---- item 2: cùng key, khác nội dung -> từ chối, không tạo đơn thứ hai ----

    @Test
    void should_return422Mismatch_when_sameKeyDifferentBody() throws Exception {
        Integer dishId = fixtures.createDish(adminToken, restaurantId, 10);
        SystemTestFixtures.RegisteredUser user = fixtures.registerUser();
        Integer addressOne = fixtures.createAddress(user.accessToken(), "Address One");
        Integer addressTwo = fixtures.createAddress(user.accessToken(), "Address Two");
        fixtures.addToCart(user.accessToken(), dishId, 1);

        String idempotencyKey = "idem-mismatch-" + System.nanoTime();

        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", SystemTestFixtures.bearer(user.accessToken()))
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("addressId", addressOne))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", SystemTestFixtures.bearer(user.accessToken()))
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("addressId", addressTwo))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("IDEMPOTENCY_KEY_REQUEST_MISMATCH"));

        MvcResult ordersResult = mockMvc.perform(get("/api/v1/orders/user")
                        .header("Authorization", SystemTestFixtures.bearer(user.accessToken())))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(objectMapper.readTree(ordersResult.getResponse().getContentAsString())).hasSize(1);
    }

    // ---- item 3 + 4: BVA quanh giới hạn 36 ký tự ----

    @Test
    void should_return400TooLong_when_keyExceeds36Characters() throws Exception {
        Integer dishId = fixtures.createDish(adminToken, restaurantId, 10);
        SystemTestFixtures.RegisteredUser user = fixtures.registerUser();
        Integer addressId = fixtures.createAddress(user.accessToken(), "Too Long Key Street");
        fixtures.addToCart(user.accessToken(), dishId, 1);

        String key37Chars = "a".repeat(37);

        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", SystemTestFixtures.bearer(user.accessToken()))
                        .header("Idempotency-Key", key37Chars)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("addressId", addressId))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("IDEMPOTENCY_KEY_TOO_LONG"));

        MvcResult ordersResult = mockMvc.perform(get("/api/v1/orders/user")
                        .header("Authorization", SystemTestFixtures.bearer(user.accessToken())))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(objectMapper.readTree(ordersResult.getResponse().getContentAsString())).isEmpty();
    }

    @Test
    void should_accept_when_keyIsExactly36Characters() throws Exception {
        Integer dishId = fixtures.createDish(adminToken, restaurantId, 10);
        SystemTestFixtures.RegisteredUser user = fixtures.registerUser();
        Integer addressId = fixtures.createAddress(user.accessToken(), "Exactly 36 Chars Street");
        fixtures.addToCart(user.accessToken(), dishId, 1);

        String key36Chars = "a".repeat(36);

        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", SystemTestFixtures.bearer(user.accessToken()))
                        .header("Idempotency-Key", key36Chars)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("addressId", addressId))))
                .andExpect(status().isCreated());
    }

    // ---- item 5: user khác dùng lại key của user A không được nhận đơn của A ----

    @Test
    void should_notLeakOrder_when_anotherUserReusesSameKey() throws Exception {
        Integer dishId = fixtures.createDish(adminToken, restaurantId, 10);
        SystemTestFixtures.RegisteredUser userA = fixtures.registerUser();
        SystemTestFixtures.RegisteredUser userB = fixtures.registerUser();
        Integer addressA = fixtures.createAddress(userA.accessToken(), "User A Street");
        Integer addressB = fixtures.createAddress(userB.accessToken(), "User B Street");
        fixtures.addToCart(userA.accessToken(), dishId, 1);
        fixtures.addToCart(userB.accessToken(), dishId, 1);

        String sharedKey = "shared-key-" + System.nanoTime();

        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", SystemTestFixtures.bearer(userA.accessToken()))
                        .header("Idempotency-Key", sharedKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("addressId", addressA))))
                .andExpect(status().isCreated());

        // Key đã bị userA giữ (reserve) trong IdempotencyKeyStore -- userB gõ trúng đúng key
        // đó nhưng userId không khớp, nên bị từ chối thẳng thay vì được replay đơn của A.
        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", SystemTestFixtures.bearer(userB.accessToken()))
                        .header("Idempotency-Key", sharedKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("addressId", addressB))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("REQUEST_IN_PROGRESS"));

        MvcResult ordersOfB = mockMvc.perform(get("/api/v1/orders/user")
                        .header("Authorization", SystemTestFixtures.bearer(userB.accessToken())))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(objectMapper.readTree(ordersOfB.getResponse().getContentAsString())).isEmpty();
    }
}
