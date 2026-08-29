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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Order.@Version (optimistic locking) phải khiến đúng một trong hai cập nhật trạng thái đồng
 * thời thắng, cái còn lại nhận ORDER_STATUS_CONFLICT thay vì âm thầm ghi đè (lost update) hoặc
 * tạo ra trạng thái lai. OrderStatusTransitionTest (Phase 1/2) chỉ kiểm máy trạng thái đơn
 * luồng bằng Mockito -- không thể chạm tới ObjectOptimisticLockingFailureException, thứ chỉ
 * JPA thật mới ném khi hai transaction cùng saveAndFlush trên một version cũ.
 */
class OrderStatusConcurrencyTest extends AbstractMySqlConcurrencyTest {

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

    // ---- item 1: admin chuyển PREPARING đồng thời khách huỷ -- đúng 1 thắng ----

    @Test
    @Timeout(25)
    void should_letExactlyOneWin_when_adminPreparesAndCustomerCancelsConcurrently() throws Exception {
        Integer dishId = fixtures.createDish(adminToken, restaurantId, 10);
        ConcurrencyTestFixtures.RegisteredUser user = fixtures.registerUser();
        Integer addressId = fixtures.createAddress(user.accessToken(), "Status Race Street", false);
        fixtures.addToCart(user.accessToken(), dishId, 3);

        int orderId = createOrder(user.accessToken(), addressId);
        int stockAfterOrder = fixtures.readDishStock(dishId);

        List<Callable<HttpStatusCode>> tasks = List.of(
                () -> fixtures.exchange("/api/v1/admin/orders/" + orderId + "/status",
                                HttpMethod.PATCH, Map.of("status", "PREPARING"), adminToken)
                        .getStatusCode(),
                () -> fixtures.exchange("/api/v1/orders/user/" + orderId + "/cancel",
                                HttpMethod.PATCH, null, user.accessToken())
                        .getStatusCode()
        );

        List<HttpStatusCode> results = ConcurrentRace.run(tasks, 20);

        assertThat(results).filteredOn(HttpStatus.OK::equals).hasSize(1);
        assertThat(results).filteredOn(HttpStatus.CONFLICT::equals).hasSize(1);

        String finalStatus = getOrderStatus(orderId);
        assertThat(finalStatus).isIn("PREPARING", "CANCELLED");

        // Tồn kho phải khớp với trạng thái cuối, không phải một con số cố định: PREPARING
        // giữ nguyên tồn kho sau khi đặt, CANCELLED phải hoàn kho về mức trước khi đặt.
        int expectedStock = finalStatus.equals("CANCELLED") ? 10 : stockAfterOrder;
        assertThat(fixtures.readDishStock(dishId)).isEqualTo(expectedStock);
    }

    // ---- item 2: hai admin cùng chuyển PREPARING -- đúng 1 thắng ----

    @Test
    @Timeout(25)
    void should_letExactlyOneWin_when_twoAdminsPrepareTheSameOrderConcurrently() throws Exception {
        Integer dishId = fixtures.createDish(adminToken, restaurantId, 10);
        ConcurrencyTestFixtures.RegisteredUser user = fixtures.registerUser();
        Integer addressId = fixtures.createAddress(user.accessToken(), "Double Admin Street", false);
        fixtures.addToCart(user.accessToken(), dishId, 2);

        int orderId = createOrder(user.accessToken(), addressId);

        List<Callable<HttpStatusCode>> tasks = List.of(
                () -> fixtures.exchange("/api/v1/admin/orders/" + orderId + "/status",
                                HttpMethod.PATCH, Map.of("status", "PREPARING"), adminToken)
                        .getStatusCode(),
                () -> fixtures.exchange("/api/v1/admin/orders/" + orderId + "/status",
                                HttpMethod.PATCH, Map.of("status", "PREPARING"), adminToken)
                        .getStatusCode()
        );

        List<HttpStatusCode> results = ConcurrentRace.run(tasks, 20);

        assertThat(results).filteredOn(HttpStatus.OK::equals).hasSize(1);
        assertThat(results).filteredOn(HttpStatus.CONFLICT::equals).hasSize(1);
        assertThat(getOrderStatus(orderId)).isEqualTo("PREPARING");
    }

    // ---- helper ----

    private int createOrder(String userToken, Integer addressId) {
        ResponseEntity<String> response = fixtures.createOrder(userToken, addressId);
        if (response.getStatusCode() != HttpStatus.CREATED) {
            throw new AssertionError("Tạo đơn thất bại: " + response.getStatusCode() + " " + response.getBody());
        }
        return fixtures.readTree(response.getBody()).get("id").asInt();
    }

    private String getOrderStatus(int orderId) {
        ResponseEntity<String> response = fixtures.authGet("/api/v1/admin/orders/" + orderId, adminToken);
        JsonNode json = fixtures.readTree(response.getBody());
        return json.get("status").asText();
    }
}
