package com.example.foodie.ordering.order;

import com.example.foodie.support.AbstractMySqlConcurrencyTest;
import com.example.foodie.support.ConcurrencyTestFixtures;
import com.example.foodie.support.ConcurrentRace;
import com.example.foodie.support.SystemTestFixtures;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import java.util.List;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OrderServiceImpl.lockDishesForUpdate sort id tăng dần TRƯỚC khi SELECT ... FOR UPDATE từng
 * dish, để hai đơn có tập dish giao nhau luôn khoá theo cùng một thứ tự thay vì khoá chéo
 * (A giữ khoá d1 chờ d2, B giữ khoá d2 chờ d1 -- deadlock kinh điển). Dựng giỏ hàng của hai
 * khách theo thứ tự thêm-vào NGƯỢC nhau (A: d1 rồi d2; B: d2 rồi d1) để nếu ai đó xoá dòng
 * `.sorted()` trong tương lai, test này bắt được ngay bằng cách treo tới
 * innodb_lock_wait_timeout thay vì âm thầm xanh giả.
 */
class DishLockOrderingConcurrencyTest extends AbstractMySqlConcurrencyTest {

    @Autowired
    private ObjectMapper objectMapper;

    private ConcurrencyTestFixtures fixtures;

    @BeforeEach
    void setUp() {
        fixtures = new ConcurrencyTestFixtures(restTemplate, objectMapper);
    }

    @AfterEach
    void tearDown() {
        SystemTestFixtures.resetOrderingData(jdbcTemplate);
    }

    @Test
    @Timeout(25)
    void should_completeBothOrders_when_cartsShareDishesInOppositeOrder() throws Exception {
        String adminToken = fixtures.adminAccessToken();
        Integer restaurantId = fixtures.createRestaurant(adminToken);

        // Tồn kho dư dả -- test này kiểm thứ tự khoá, không kiểm tranh chấp tồn kho.
        Integer d1 = fixtures.createDish(adminToken, restaurantId, 100);
        Integer d2 = fixtures.createDish(adminToken, restaurantId, 100);
        assertThat(d1).isLessThan(d2);

        ConcurrencyTestFixtures.RegisteredUser userA = fixtures.registerUser();
        ConcurrencyTestFixtures.RegisteredUser userB = fixtures.registerUser();
        Integer addressA = fixtures.createAddress(userA.accessToken(), "A - Lock Order Street", false);
        Integer addressB = fixtures.createAddress(userB.accessToken(), "B - Lock Order Street", false);

        // A: d1 trước, d2 sau. B: d2 trước, d1 sau -- thứ tự đối nghịch thật sự.
        fixtures.addToCart(userA.accessToken(), d1, 1);
        fixtures.addToCart(userA.accessToken(), d2, 1);
        fixtures.addToCart(userB.accessToken(), d2, 1);
        fixtures.addToCart(userB.accessToken(), d1, 1);

        List<Callable<HttpStatusCode>> tasks = List.of(
                () -> fixtures.createOrder(userA.accessToken(), addressA).getStatusCode(),
                () -> fixtures.createOrder(userB.accessToken(), addressB).getStatusCode()
        );

        // Timeout 15s (< innodb_lock_wait_timeout mặc định 50s) đúng theo đặc tả -- nếu thứ tự
        // khoá bị phá, ConcurrentRace ném AssertionError ở đây thay vì để build treo tới 50s.
        List<HttpStatusCode> results = ConcurrentRace.run(tasks, 15);

        assertThat(results)
                .as("cả hai đơn phải tạo thành công, không đơn nào chết vì deadlock/lock timeout")
                .containsExactlyInAnyOrder(HttpStatus.CREATED, HttpStatus.CREATED);
    }
}
