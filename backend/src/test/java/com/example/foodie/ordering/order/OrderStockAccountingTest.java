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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Bất biến trung tâm của hệ thống đặt món: tồn_kho + tổng_số_lượng_trong_đơn_chưa_huỷ = hằng
 * số. Mockito (Phase 2) không thể chứng minh điều này -- nó chỉ xác nhận "có gọi
 * setStockQuantity", không xác nhận transaction thật đã commit đúng giá trị. Ở đây mọi
 * assertion đọc lại tồn kho qua GET /dishes công khai (external state), không verify() lời gọi.
 */
class OrderStockAccountingTest extends AbstractMySqlIntegrationTest {

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

    // Không @Transactional trên test (bắt buộc của Phase 5) nên dữ liệu tạo qua HTTP commit
    // thật, vĩnh viễn -- container MySQL dùng chung với Phase 4, nơi
    // RestaurantRepositoryTest.countActiveRestaurants() đếm KHÔNG lọc phạm vi. Không dọn,
    // chạy `mvn test` toàn bộ suite sẽ làm test đó đỏ dù bản thân nó không đổi gì.
    @AfterEach
    void tearDown() {
        SystemTestFixtures.resetOrderingData(jdbcTemplate);
    }

    // ---- item 1 + 2: tạo đơn trừ kho + giỏ rỗng, huỷ đơn hoàn kho ----

    @Test
    void should_decrementStockAndEmptyCart_thenRestoreStockOnCancel() throws Exception {
        Integer dishId = fixtures.createDish(adminToken, restaurantId, 10);
        SystemTestFixtures.RegisteredUser user = fixtures.registerUser();
        Integer addressId = fixtures.createAddress(user.accessToken(), "123 Test Street");
        fixtures.addToCart(user.accessToken(), dishId, 3);

        MvcResult createResult = mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", SystemTestFixtures.bearer(user.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("addressId", addressId))))
                .andExpect(status().isCreated())
                .andReturn();
        int orderId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asInt();

        assertThat(fixtures.readDishStock(dishId)).isEqualTo(7);
        assertCartSize(user.accessToken(), 0);

        // ---- item 2: huỷ khi còn PENDING -> hoàn kho ----
        mockMvc.perform(patch("/api/v1/orders/user/" + orderId + "/cancel")
                        .header("Authorization", SystemTestFixtures.bearer(user.accessToken())))
                .andExpect(status().isOk());

        assertThat(fixtures.readDishStock(dishId)).isEqualTo(10);
    }

    // ---- item 3: all-or-nothing khi có món bị khoá giữa chừng ----

    @Test
    void should_rollbackEverything_when_oneDishBecomesUnavailable() throws Exception {
        Integer availableDishId = fixtures.createDish(adminToken, restaurantId, 10);
        Integer blockedDishId = fixtures.createDish(adminToken, restaurantId, 10);
        SystemTestFixtures.RegisteredUser user = fixtures.registerUser();
        Integer addressId = fixtures.createAddress(user.accessToken(), "456 Test Street");

        fixtures.addToCart(user.accessToken(), availableDishId, 2);
        fixtures.addToCart(user.accessToken(), blockedDishId, 1);

        // Khoá món thứ hai SAU khi đã nằm trong giỏ -- mô phỏng đúng tình huống admin
        // ẩn món trong lúc khách đang thao tác.
        mockMvc.perform(post("/api/v1/admin/dishes/blocking/" + blockedDishId + "/0")
                        .header("Authorization", SystemTestFixtures.bearer(adminToken)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", SystemTestFixtures.bearer(user.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("addressId", addressId))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("DISH_NOT_AVAILABLE"));

        assertThat(fixtures.readDishStock(availableDishId)).isEqualTo(10);
        assertCartSize(user.accessToken(), 2);
        assertThat(hasNoOrders(user.accessToken())).isTrue();
    }

    // ---- item 4: biên sl == kho ----

    @Test
    void should_allowOrder_when_quantityEqualsStock() throws Exception {
        Integer dishId = fixtures.createDish(adminToken, restaurantId, 5);
        SystemTestFixtures.RegisteredUser user = fixtures.registerUser();
        Integer addressId = fixtures.createAddress(user.accessToken(), "789 Test Street");
        fixtures.addToCart(user.accessToken(), dishId, 5);

        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", SystemTestFixtures.bearer(user.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("addressId", addressId))))
                .andExpect(status().isCreated());

        assertThat(fixtures.readDishStock(dishId)).isEqualTo(0);
    }

    // ---- item 5: kho tụt xuống dưới số lượng trong giỏ SAU khi đã thêm vào giỏ ----
    // addUserDish tự chặn thêm-vào-giỏ vượt tồn kho, nên không thể tạo trực tiếp giỏ có
    // sl 6 khi kho chỉ còn 5. Dựng bằng cách: thêm 6 vào giỏ lúc kho còn 6, rồi một user
    // KHÁC mua 1 đơn vị (tuần tự, không đồng thời -- Phase 6 mới kiểm race condition) để
    // kho tụt xuống 5, sau đó user đầu mới tạo đơn.

    @Test
    void should_rejectOrder_when_stockDropsBelowCartQuantityAfterAdding() throws Exception {
        Integer dishId = fixtures.createDish(adminToken, restaurantId, 6);

        SystemTestFixtures.RegisteredUser userA = fixtures.registerUser();
        fixtures.addToCart(userA.accessToken(), dishId, 6);

        SystemTestFixtures.RegisteredUser userB = fixtures.registerUser();
        Integer addressB = fixtures.createAddress(userB.accessToken(), "B's address");
        fixtures.addToCart(userB.accessToken(), dishId, 1);
        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", SystemTestFixtures.bearer(userB.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("addressId", addressB))))
                .andExpect(status().isCreated());
        assertThat(fixtures.readDishStock(dishId)).isEqualTo(5);

        Integer addressA = fixtures.createAddress(userA.accessToken(), "A's address");
        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", SystemTestFixtures.bearer(userA.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("addressId", addressA))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("DISH_OUT_OF_STOCK"));

        assertThat(fixtures.readDishStock(dishId)).isEqualTo(5);
        assertCartSize(userA.accessToken(), 1);
    }

    // ---- helper ----

    private void assertCartSize(String userToken, int expectedSize) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/user-dishes")
                        .header("Authorization", SystemTestFixtures.bearer(userToken)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode cart = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(cart).hasSize(expectedSize);
    }

    private boolean hasNoOrders(String userToken) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/orders/user")
                        .header("Authorization", SystemTestFixtures.bearer(userToken)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode orders = objectMapper.readTree(result.getResponse().getContentAsString());
        return orders.isEmpty();
    }
}
