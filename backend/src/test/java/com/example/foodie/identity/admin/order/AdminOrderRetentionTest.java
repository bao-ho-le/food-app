package com.example.foodie.identity.admin.order;

import com.example.foodie.support.AbstractMySqlIntegrationTest;
import com.example.foodie.support.SystemTestFixtures;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Đơn đã giao (DELIVERED) là dữ liệu kế toán, phải giữ cho audit/reporting -- xoá dữ liệu
 * kiểu này cần cơ chế archival riêng, không phải DELETE thông thường (xem comment trong
 * OrderServiceImpl.deleteById). Test này giữ hàng rào chống việc quy tắc đó bị nới lỏng.
 */
class AdminOrderRetentionTest extends AbstractMySqlIntegrationTest {

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

    // ---- item 1: xoá đơn DELIVERED bị từ chối, đơn vẫn còn trong DB ----

    @Test
    void should_forbidDelete_when_orderIsDelivered() throws Exception {
        int orderId = createOrder();
        advanceStatus(orderId, "PREPARING");
        advanceStatus(orderId, "DELIVERING");
        advanceStatus(orderId, "DELIVERED");

        mockMvc.perform(delete("/api/v1/admin/orders/" + orderId)
                        .header("Authorization", SystemTestFixtures.bearer(adminToken)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("ORDER_DELETE_FORBIDDEN"));

        mockMvc.perform(get("/api/v1/admin/orders/" + orderId)
                        .header("Authorization", SystemTestFixtures.bearer(adminToken)))
                .andExpect(status().isOk());
    }

    // ---- item 2: xoá đơn PENDING thành công, đơn không còn trong DB ----

    @Test
    void should_allowDelete_when_orderIsPending() throws Exception {
        int orderId = createOrder();

        mockMvc.perform(delete("/api/v1/admin/orders/" + orderId)
                        .header("Authorization", SystemTestFixtures.bearer(adminToken)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/admin/orders/" + orderId)
                        .header("Authorization", SystemTestFixtures.bearer(adminToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("ORDER_NOT_FOUND"));
    }

    // ---- helper ----

    private int createOrder() throws Exception {
        Integer dishId = fixtures.createDish(adminToken, restaurantId, 10);
        SystemTestFixtures.RegisteredUser user = fixtures.registerUser();
        Integer addressId = fixtures.createAddress(user.accessToken(), "Retention Street " + System.nanoTime());
        fixtures.addToCart(user.accessToken(), dishId, 1);

        MvcResult result = mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", SystemTestFixtures.bearer(user.accessToken()))
                        .header("Idempotency-Key", "retention-" + System.nanoTime())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("addressId", addressId))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asInt();
    }

    private void advanceStatus(int orderId, String status) throws Exception {
        mockMvc.perform(patch("/api/v1/admin/orders/" + orderId + "/status")
                        .header("Authorization", SystemTestFixtures.bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", status))))
                .andExpect(status().isOk());
    }
}
