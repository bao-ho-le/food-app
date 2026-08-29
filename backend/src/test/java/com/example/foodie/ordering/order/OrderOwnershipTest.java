package com.example.foodie.ordering.order;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * IDOR (Insecure Direct Object Reference) trên đơn hàng và địa chỉ: một user chỉ được thao
 * tác trên tài nguyên của chính mình, dù id hợp lệ và tồn tại thật trong hệ thống.
 */
class OrderOwnershipTest extends AbstractMySqlIntegrationTest {

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

    // ---- item 1: addressId thuộc user khác -- chống rò rỉ địa chỉ nhà qua duyệt id ----
    // Nếu hệ thống chỉ kiểm "address có tồn tại" mà không kiểm chủ sở hữu, kẻ tấn công duyệt
    // addressId tuần tự là đọc được (gián tiếp, qua tạo đơn thành công hay không) địa chỉ nhà
    // của mọi người dùng khác.

    @Test
    void should_return404AddressNotFound_when_addressBelongsToAnotherUser() throws Exception {
        Integer dishId = fixtures.createDish(adminToken, restaurantId, 10);
        SystemTestFixtures.RegisteredUser userA = fixtures.registerUser();
        SystemTestFixtures.RegisteredUser userB = fixtures.registerUser();

        String secretAddressOfB = "Secret Home of B " + System.nanoTime();
        Integer addressOfB = fixtures.createAddress(userB.accessToken(), secretAddressOfB);
        fixtures.addToCart(userA.accessToken(), dishId, 1);

        MvcResult result = mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", SystemTestFixtures.bearer(userA.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("addressId", addressOfB))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("ADDRESS_NOT_FOUND"))
                .andReturn();

        assertThat(result.getResponse().getContentAsString()).doesNotContain(secretAddressOfB);

        MvcResult ordersOfA = mockMvc.perform(get("/api/v1/orders/user")
                        .header("Authorization", SystemTestFixtures.bearer(userA.accessToken())))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(objectMapper.readTree(ordersOfA.getResponse().getContentAsString())).isEmpty();
    }

    // ---- item 2: A huỷ đơn của B ----

    @Test
    void should_return403_when_userCancelsAnotherUsersOrder() throws Exception {
        int orderIdOfB = createOrderFor(fixtures.registerUser());
        SystemTestFixtures.RegisteredUser userA = fixtures.registerUser();

        mockMvc.perform(patch("/api/v1/orders/user/" + orderIdOfB + "/cancel")
                        .header("Authorization", SystemTestFixtures.bearer(userA.accessToken())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ORDER_NOT_OWNER"));
    }

    // ---- item 3: A xem chi tiết đơn của B ----

    @Test
    void should_return403_when_userViewsAnotherUsersOrderDetails() throws Exception {
        int orderIdOfB = createOrderFor(fixtures.registerUser());
        SystemTestFixtures.RegisteredUser userA = fixtures.registerUser();

        mockMvc.perform(get("/api/v1/orders/user/" + orderIdOfB)
                        .header("Authorization", SystemTestFixtures.bearer(userA.accessToken())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ORDER_NOT_OWNER"));
    }

    // ---- item 4: orderId không tồn tại -- phải 404, không phải 403 ----
    // Thứ tự kiểm tra là một phần của hợp đồng: tồn tại trước, quyền sau. Nếu đảo ngược,
    // chênh lệch 403/404 giữa id có thật và id giả sẽ tiết lộ id nào tồn tại thật.

    @Test
    void should_return404NotOwner403_when_orderIdDoesNotExist() throws Exception {
        SystemTestFixtures.RegisteredUser user = fixtures.registerUser();

        mockMvc.perform(patch("/api/v1/orders/user/999999999/cancel")
                        .header("Authorization", SystemTestFixtures.bearer(user.accessToken())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("ORDER_NOT_FOUND"));
    }

    // ---- helper ----

    private int createOrderFor(SystemTestFixtures.RegisteredUser user) throws Exception {
        Integer dishId = fixtures.createDish(adminToken, restaurantId, 10);
        Integer addressId = fixtures.createAddress(user.accessToken(), "Owner Street " + System.nanoTime());
        fixtures.addToCart(user.accessToken(), dishId, 1);

        MvcResult result = mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", SystemTestFixtures.bearer(user.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("addressId", addressId))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asInt();
    }
}
