package com.example.foodie.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockCookie;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Fixture dùng chung cho toàn bộ test Phase 5 -- đi qua HTTP thật (MockMvc, filter chain thật)
 * thay vì gọi thẳng service/repository, vì Phase 5 tồn tại để chứng minh hành vi đầu-cuối.
 * Sáu test class dùng lại các bước này thay vì tự lặp lại "đăng ký user rồi lấy token",
 * "tạo nhà hàng/món ăn", "thêm vào giỏ"...
 */
public class SystemTestFixtures {

    private static final String USERS = "/api/v1/users";
    private static final String ADMIN_RESTAURANTS = "/api/v1/admin/restaurants";
    private static final String ADMIN_DISHES = "/api/v1/admin/dishes";
    private static final String USER_DISHES = "/api/v1/user-dishes";
    private static final String ADDRESS = "/api/v1/address";
    private static final String DISHES = "/api/v1/dishes";

    public static final String COOKIE_NAME = "refreshToken";
    public static final String ADMIN_EMAIL = "admin@test.local";
    public static final String ADMIN_PASSWORD = "ChangeMe123";

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    public SystemTestFixtures(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    public record RegisteredUser(String accessToken, String refreshTokenCookie, String email, String phoneNumber) {
    }

    public RegisteredUser registerUser() throws Exception {
        String email = "sys-" + System.nanoTime() + "@test.local";
        String phone = randomVietnamesePhone();

        Map<String, Object> body = Map.of(
                "fullName", "System Test User",
                "gender", "MALE",
                "phoneNumber", phone,
                "email", email,
                "password", "SuperSecret123"
        );

        MvcResult result = mockMvc.perform(post(USERS + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        String refreshCookie = extractCookie(result.getResponse());
        return new RegisteredUser(json.get("accessToken").asText(), refreshCookie, email, phone);
    }

    public String adminAccessToken() throws Exception {
        Map<String, Object> body = Map.of("email", ADMIN_EMAIL, "password", ADMIN_PASSWORD);

        MvcResult result = mockMvc.perform(post(USERS + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    }

    public Integer createRestaurant(String adminToken) throws Exception {
        // existsByPhoneNumber(null) khớp IS NULL (hành vi derived query của Spring Data) --
        // nếu bỏ trống phoneNumber, nhà hàng thứ hai trở đi sẽ đụng
        // RESTAURANT_PHONE_ALREADY_EXISTS vì "trùng" với nhà hàng null trước đó.
        Map<String, Object> body = Map.of(
                "name", "Sys Test Restaurant " + System.nanoTime(),
                "phoneNumber", randomVietnamesePhone(),
                "isAvailable", true);

        MvcResult result = mockMvc.perform(post(ADMIN_RESTAURANTS)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asInt();
    }

    /** Tạo món ăn (kho mặc định 0) rồi restock lên đúng {@code stock} nếu > 0. */
    public Integer createDish(String adminToken, Integer restaurantId, int stock) throws Exception {
        // tags chỉ cần khác null (DishHelper.validateDishRequest), rỗng là hợp lệ.
        // imageUrl BẮT BUỘC phải có giá trị: DishServiceImpl.attachImage() luôn tạo một Image
        // gắn với dish mới, kể cả khi imageUrl null -- Image.url lại @NotNull, nên bỏ trống
        // imageUrl làm createDish luôn 400 CONSTRAINT_VIOLATION (bug thật, ngoài phạm vi Phase 5).
        Map<String, Object> body = Map.of(
                "name", "Sys Test Dish " + System.nanoTime(),
                "price", 10_000,
                "restaurantId", restaurantId,
                "tags", java.util.List.of(),
                "imageUrl", "https://example.test/dish.jpg"
        );

        MvcResult result = mockMvc.perform(post(ADMIN_DISHES)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andReturn();

        int dishId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asInt();

        if (stock > 0) {
            mockMvc.perform(post(ADMIN_DISHES + "/" + dishId + "/stock")
                            .header("Authorization", bearer(adminToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of("quantity", stock))))
                    .andExpect(status().isOk());
        }
        return dishId;
    }

    public void addToCart(String userToken, Integer dishId, int quantity) throws Exception {
        mockMvc.perform(post(USER_DISHES)
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("dishId", dishId, "quantity", quantity))))
                .andExpect(status().isCreated());
    }

    public Integer createAddress(String userToken, String addressLine) throws Exception {
        Map<String, Object> body = Map.of("address", addressLine, "isDefault", false);

        MvcResult result = mockMvc.perform(post(ADDRESS + "/user")
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asInt();
    }

    /** Đọc tồn kho hiện tại qua endpoint công khai GET /dishes -- không đụng repository. */
    public int readDishStock(Integer dishId) throws Exception {
        MvcResult result = mockMvc.perform(get(DISHES)).andExpect(status().isOk()).andReturn();
        JsonNode dishes = objectMapper.readTree(result.getResponse().getContentAsString());
        for (JsonNode dish : dishes) {
            if (dish.get("id").asInt() == dishId) {
                return dish.get("stockQuantity").asInt();
            }
        }
        throw new AssertionError("Không tìm thấy dish id=" + dishId + " trong GET /dishes");
    }

    /**
     * Xoá sạch dữ liệu order/dish/restaurant sau mỗi test. Phase 5 KHÔNG dùng @Transactional
     * trên test (bắt buộc, xem javadoc lớp) nên mọi thứ tạo qua HTTP đều commit thật và tồn
     * tại vĩnh viễn trong container MySQL dùng chung với Phase 4. Không cleanup, các query
     * đếm/tổng không lọc theo phạm vi test (RestaurantRepositoryTest.countActiveRestaurants,
     * OrderRepositoryTest.sumTotalPriceByStatusAndCreatedAtBetween,
     * OrderDishRepositoryTest.sumQuantityByDishForOrderStatus) sẽ đếm nhầm cả rác của Phase 5
     * -- đã xác nhận bằng thực nghiệm (mvn test toàn bộ suite làm 3 test đó đỏ dù bản thân
     * chúng không đổi). Thứ tự xoá tuân theo chiều FK (con trước cha); order_dish.dish_id là
     * ON DELETE RESTRICT nên phải xoá trước dish.
     */
    public static void resetOrderingData(JdbcTemplate jdbc) {
        jdbc.update("DELETE FROM order_dish");
        jdbc.update("DELETE FROM orders");
        jdbc.update("DELETE FROM user_dish");
        jdbc.update("DELETE FROM dish");
        jdbc.update("DELETE FROM restaurant");
    }

    public static String bearer(String token) {
        return "Bearer " + token;
    }

    public static MockCookie refreshCookie(String value) {
        return new MockCookie(COOKIE_NAME, value);
    }

    public static String extractCookie(MockHttpServletResponse response) {
        var cookie = response.getCookie(COOKIE_NAME);
        return cookie != null ? cookie.getValue() : null;
    }

    public static String randomVietnamesePhone() {
        // "0" + 9 chữ số ngẫu nhiên, khớp regex ^(0|\+84)(\d{9})$ -- cùng công thức với
        // RefreshTokenReuseDetectionTest để không đụng số của test đó khi chạy chung context.
        long n = ThreadLocalRandom.current().nextLong(100_000_000L, 999_999_999L);
        return "0" + n;
    }
}
