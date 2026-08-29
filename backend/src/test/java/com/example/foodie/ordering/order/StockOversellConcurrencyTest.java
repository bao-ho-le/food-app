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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Bất biến trung tâm: không bao giờ bán vượt tồn kho, và tồn_kho + tổng_đã_bán là hằng số --
 * OrderStockAccountingTest (Phase 5) đã chứng minh điều này đúng khi các request TUẦN TỰ. Ở
 * đây hai/nhiều request chạm CÙNG một dòng dish thật sự đồng thời (CountDownLatch), thứ duy
 * nhất đứng giữa là DishRepository.findByIdForUpdate (SELECT ... FOR UPDATE) -- nếu khoá bi
 * quan đó bị gỡ, cả hai luồng sẽ đọc cùng một giá trị tồn kho cũ và cùng trừ, gây bán vượt kho
 * hoặc đâm vào CHECK (stock_quantity >= 0) và trả 500.
 */
class StockOversellConcurrencyTest extends AbstractMySqlConcurrencyTest {

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

    // Không @Transactional trên test (bắt buộc, xem AbstractMySqlConcurrencyTest) nên dữ liệu
    // tạo qua HTTP commit thật -- dọn để không làm sai các query đếm/tổng không lọc phạm vi ở
    // Phase 4 (xem javadoc SystemTestFixtures.resetOrderingData).
    @AfterEach
    void tearDown() {
        SystemTestFixtures.resetOrderingData(jdbcTemplate);
    }

    // ---- item 1: tồn kho 1, hai khách cùng đặt 1 -- race quan trọng nhất của cả phase ----

    @Test
    @Timeout(30)
    void should_sellExactlyOnce_when_twoCustomersRaceForTheLastUnit() throws Exception {
        Integer dishId = fixtures.createDish(adminToken, restaurantId, 1);

        ConcurrencyTestFixtures.RegisteredUser userA = fixtures.registerUser();
        ConcurrencyTestFixtures.RegisteredUser userB = fixtures.registerUser();
        Integer addressA = fixtures.createAddress(userA.accessToken(), "A - Last Unit Street", false);
        Integer addressB = fixtures.createAddress(userB.accessToken(), "B - Last Unit Street", false);
        fixtures.addToCart(userA.accessToken(), dishId, 1);
        fixtures.addToCart(userB.accessToken(), dishId, 1);

        List<Callable<HttpStatusCode>> tasks = List.of(
                createOrderTask(userA.accessToken(), addressA),
                createOrderTask(userB.accessToken(), addressB)
        );
        List<HttpStatusCode> results = ConcurrentRace.run(tasks, 20);

        assertThat(results).filteredOn(HttpStatus.CREATED::equals).hasSize(1);
        assertThat(results).filteredOn(HttpStatus.CONFLICT::equals).hasSize(1);
        assertThat(fixtures.readDishStock(dishId)).isEqualTo(0);

        Integer orderCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM order_dish WHERE dish_id = ?", Integer.class, dishId);
        assertThat(orderCount).as("chỉ đúng 1 đơn thật sự chứa dish này").isEqualTo(1);
    }

    // ---- item 2: tồn kho 5, năm khách cùng đặt 1 -- không mất update ----

    @Test
    @Timeout(30)
    void should_sellAllFive_when_fiveCustomersOrderConcurrently() throws Exception {
        Integer dishId = fixtures.createDish(adminToken, restaurantId, 5);

        List<Callable<HttpStatusCode>> tasks = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            ConcurrencyTestFixtures.RegisteredUser user = fixtures.registerUser();
            Integer addressId = fixtures.createAddress(user.accessToken(), "Five Customers Street " + i, false);
            fixtures.addToCart(user.accessToken(), dishId, 1);
            tasks.add(createOrderTask(user.accessToken(), addressId));
        }

        List<HttpStatusCode> results = ConcurrentRace.run(tasks, 20);

        assertThat(results).allMatch(HttpStatus.CREATED::equals);
        assertThat(fixtures.readDishStock(dishId)).isEqualTo(0);

        Integer orderCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM order_dish WHERE dish_id = ?", Integer.class, dishId);
        assertThat(orderCount).isEqualTo(5);
    }

    // ---- item 3: đặt hàng (-4) đồng thời admin nhập kho (+6) -- không mất update ----

    @Test
    @Timeout(30)
    void should_notLoseUpdate_when_orderingAndRestockingConcurrently() throws Exception {
        Integer dishId = fixtures.createDish(adminToken, restaurantId, 10);

        ConcurrencyTestFixtures.RegisteredUser user = fixtures.registerUser();
        Integer addressId = fixtures.createAddress(user.accessToken(), "Restock Race Street", false);
        fixtures.addToCart(user.accessToken(), dishId, 4);

        List<Callable<HttpStatusCode>> tasks = List.of(
                createOrderTask(user.accessToken(), addressId),
                () -> fixtures.post("/api/v1/admin/dishes/" + dishId + "/stock", Map.of("quantity", 6), adminToken)
                        .getStatusCode()
        );

        List<HttpStatusCode> results = ConcurrentRace.run(tasks, 20);

        // createOrder trả 201, restock trả 200 -- cả hai đều luôn thành công (không tranh
        // chấp business rule nào, chỉ tranh chấp khoá pessimistic trên cùng dòng dish).
        assertThat(results).as("đặt hàng và nhập kho đều phải thành công").containsExactlyInAnyOrder(
                HttpStatus.CREATED, HttpStatus.OK);
        assertThat(fixtures.readDishStock(dishId)).as("10 - 4 + 6, bất kể thứ tự thực thi").isEqualTo(12);
    }

    // ---- helper ----

    private Callable<HttpStatusCode> createOrderTask(String userToken, Integer addressId) {
        return () -> fixtures.createOrder(userToken, addressId).getStatusCode();
    }
}
