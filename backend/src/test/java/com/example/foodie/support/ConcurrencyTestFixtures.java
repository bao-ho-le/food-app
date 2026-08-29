package com.example.foodie.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Bản tương đương SystemTestFixtures (Phase 5) nhưng đi qua TestRestTemplate/HTTP thật thay vì
 * MockMvc -- Phase 6 CẤM dùng MockMvc từ nhiều luồng (không thread-safe cho mục đích đó), nên
 * bước dựng dữ liệu tuần tự trước mỗi race (đăng ký user, tạo nhà hàng/món, thêm giỏ, tạo địa
 * chỉ) cũng dùng lại đúng một cách gọi để nhất quán, thay vì trộn hai cơ chế HTTP khác nhau
 * trong cùng một test.
 */
public class ConcurrencyTestFixtures {

    private static final String USERS = "/api/v1/users";
    private static final String ADMIN_RESTAURANTS = "/api/v1/admin/restaurants";
    private static final String ADMIN_DISHES = "/api/v1/admin/dishes";
    private static final String USER_DISHES = "/api/v1/user-dishes";
    private static final String ADDRESS = "/api/v1/address";
    private static final String DISHES = "/api/v1/dishes";

    public static final String COOKIE_NAME = "refreshToken";
    public static final String ADMIN_EMAIL = "admin@test.local";
    public static final String ADMIN_PASSWORD = "ChangeMe123";

    private final TestRestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public ConcurrencyTestFixtures(TestRestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public record RegisteredUser(String accessToken, String refreshTokenCookie, String email) {
    }

    public RegisteredUser registerUser() {
        Map<String, Object> body = Map.of(
                "fullName", "Concurrency Test User",
                "gender", "MALE",
                "phoneNumber", randomVietnamesePhone(),
                "email", "cct-" + System.nanoTime() + "@test.local",
                "password", "SuperSecret123"
        );

        ResponseEntity<String> response = post(USERS + "/register", body, null);
        if (response.getStatusCode() != HttpStatus.CREATED) {
            throw new AssertionError("registerUser thất bại: " + response.getStatusCode() + " " + response.getBody());
        }
        JsonNode json = readTree(response.getBody());
        return new RegisteredUser(json.get("accessToken").asText(), extractCookie(response), json.get("email").asText());
    }

    public String adminAccessToken() {
        Map<String, Object> body = Map.of("email", ADMIN_EMAIL, "password", ADMIN_PASSWORD);

        ResponseEntity<String> response = post(USERS + "/login", body, null);
        if (response.getStatusCode() != HttpStatus.OK) {
            throw new AssertionError("adminAccessToken thất bại: " + response.getStatusCode() + " " + response.getBody());
        }
        return readTree(response.getBody()).get("accessToken").asText();
    }

    public Integer createRestaurant(String adminToken) {
        Map<String, Object> body = Map.of(
                "name", "Concurrency Test Restaurant " + System.nanoTime(),
                "phoneNumber", randomVietnamesePhone(),
                "isAvailable", true);

        ResponseEntity<String> response = post(ADMIN_RESTAURANTS, body, adminToken);
        if (response.getStatusCode() != HttpStatus.OK) {
            throw new AssertionError("createRestaurant thất bại: " + response.getStatusCode() + " " + response.getBody());
        }
        return readTree(response.getBody()).get("id").asInt();
    }

    /** Tạo món ăn (kho mặc định 0) rồi restock lên đúng {@code stock} nếu > 0. */
    public Integer createDish(String adminToken, Integer restaurantId, int stock) {
        Map<String, Object> body = Map.of(
                "name", "Concurrency Test Dish " + System.nanoTime(),
                "price", 10_000,
                "restaurantId", restaurantId,
                "tags", List.of(),
                "imageUrl", "https://example.test/dish.jpg"
        );

        ResponseEntity<String> response = post(ADMIN_DISHES, body, adminToken);
        if (response.getStatusCode() != HttpStatus.OK) {
            throw new AssertionError("createDish thất bại: " + response.getStatusCode() + " " + response.getBody());
        }
        int dishId = readTree(response.getBody()).get("id").asInt();

        if (stock > 0) {
            ResponseEntity<String> stockResponse = post(ADMIN_DISHES + "/" + dishId + "/stock",
                    Map.of("quantity", stock), adminToken);
            if (stockResponse.getStatusCode() != HttpStatus.OK) {
                throw new AssertionError("restock thất bại: " + stockResponse.getStatusCode() + " " + stockResponse.getBody());
            }
        }
        return dishId;
    }

    /**
     * Idempotency-Key giờ là bắt buộc cho POST /orders (IdempotencyService.execute ném
     * IDEMPOTENCY_KEY_REQUIRED nếu thiếu) -- test nào không tự kiểm cơ chế idempotency thì
     * chỉ cần một key ngẫu nhiên, duy nhất cho mỗi lần gọi để không vô tình đụng test kia.
     */
    public ResponseEntity<String> createOrder(String userToken, Integer addressId) {
        return postWithIdempotencyKey("/api/v1/orders", Map.of("addressId", addressId),
                userToken, UUID.randomUUID().toString());
    }

    public void addToCart(String userToken, Integer dishId, int quantity) {
        ResponseEntity<String> response = post(USER_DISHES, Map.of("dishId", dishId, "quantity", quantity), userToken);
        if (response.getStatusCode() != HttpStatus.CREATED) {
            throw new AssertionError("addToCart thất bại: " + response.getStatusCode() + " " + response.getBody());
        }
    }

    public Integer createAddress(String userToken, String addressLine, boolean isDefault) {
        Map<String, Object> body = Map.of("address", addressLine, "isDefault", isDefault);

        ResponseEntity<String> response = post(ADDRESS + "/user", body, userToken);
        if (response.getStatusCode() != HttpStatus.CREATED) {
            throw new AssertionError("createAddress thất bại: " + response.getStatusCode() + " " + response.getBody());
        }
        return readTree(response.getBody()).get("id").asInt();
    }

    /** Đọc tồn kho hiện tại qua endpoint công khai GET /dishes -- không đụng repository. */
    public int readDishStock(Integer dishId) {
        ResponseEntity<String> response = restTemplate.getForEntity(DISHES, String.class);
        JsonNode dishes = readTree(response.getBody());
        for (JsonNode dish : dishes) {
            if (dish.get("id").asInt() == dishId) {
                return dish.get("stockQuantity").asInt();
            }
        }
        throw new AssertionError("Không tìm thấy dish id=" + dishId + " trong GET /dishes");
    }

    public ResponseEntity<String> post(String path, Object body, String bearerToken) {
        return exchange(path, HttpMethod.POST, body, bearerToken);
    }

    public ResponseEntity<String> exchange(String path, HttpMethod method, Object body, String bearerToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (bearerToken != null) {
            headers.set("Authorization", bearer(bearerToken));
        }
        return restTemplate.exchange(path, method, new HttpEntity<>(body, headers), String.class);
    }

    public ResponseEntity<String> authGet(String path, String bearerToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", bearer(bearerToken));
        return restTemplate.exchange(path, HttpMethod.GET, new HttpEntity<>(headers), String.class);
    }

    /** Gửi Cookie header thủ công -- TestRestTemplate không có API cookie() như MockMvc. */
    public ResponseEntity<String> postWithCookie(String path, String cookieName, String cookieValue) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.COOKIE, cookieName + "=" + cookieValue);
        return restTemplate.exchange(path, HttpMethod.POST, new HttpEntity<>(headers), String.class);
    }

    public ResponseEntity<String> postWithIdempotencyKey(String path, Object body, String bearerToken, String idempotencyKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", bearer(bearerToken));
        headers.set("Idempotency-Key", idempotencyKey);
        return restTemplate.exchange(path, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
    }

    public JsonNode readTree(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            throw new AssertionError("Không parse được JSON: " + json, e);
        }
    }

    public static String bearer(String token) {
        return "Bearer " + token;
    }

    public static String extractCookie(ResponseEntity<?> response) {
        List<String> setCookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        if (setCookies == null) {
            return null;
        }
        for (String setCookie : setCookies) {
            if (setCookie.startsWith(COOKIE_NAME + "=")) {
                String value = setCookie.substring((COOKIE_NAME + "=").length());
                int semicolon = value.indexOf(';');
                return semicolon >= 0 ? value.substring(0, semicolon) : value;
            }
        }
        return null;
    }

    public static String randomVietnamesePhone() {
        long n = ThreadLocalRandom.current().nextLong(100_000_000L, 999_999_999L);
        return "0" + n;
    }
}
