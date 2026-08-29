package com.example.foodie.ordering.userdish;

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
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * ⚠️ DỰ KIẾN ĐỎ -- thí nghiệm xác nhận, đã đọc code xác nhận nguyên nhân nhưng CHƯA sửa.
 *
 * Đặc tả: mỗi cặp (user, dish) có tối đa MỘT dòng trong giỏ hàng; thêm cùng một món hai lần
 * cộng dồn số lượng vào dòng đó.
 *
 * Nguyên nhân đã xác nhận bằng đọc code (không phải suy luận): bảng user_dish không có ràng
 * buộc UNIQUE (user_id, dish_id), và UserDishServiceImpl.addUserDish không có @Transactional
 * bao ngoài đoạn find-rồi-save. Hai request đồng thời cùng chạy đến
 * findByUser_IdAndDish_Id -- CẢ HAI đều thấy Optional.empty() (dòng còn chưa tồn tại), nên cả
 * hai đều INSERT một dòng mới thay vì một dòng cộng dồn thành 2 dòng riêng.
 *
 * Hậu quả nghiêm trọng hơn bản thân việc trùng dòng: findByUser_IdAndDish_Id trả về Optional
 * (kỳ vọng ngầm định tối đa 1 kết quả) -- mọi lời gọi SAU đó (update quantity, xoá, thêm nữa)
 * sẽ ném IncorrectResultSizeDataAccessException ⇒ 500 KHÔNG tự phục hồi, và người dùng không
 * có cách nào tự sửa vì đường xoá cũng đi qua chính repository method đó. Vì vậy test assert
 * CẢ HAI: số dòng trong DB, và việc gọi lại API sau đó vẫn hoạt động (200), dùng assertAll để
 * cả hai vế đều được báo cáo dù vế nào đỏ trước.
 */
class CartConcurrencyTest extends AbstractMySqlConcurrencyTest {

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
    void should_mergeIntoOneRow_when_sameDishAddedConcurrently() throws Exception {
        String adminToken = fixtures.adminAccessToken();
        Integer restaurantId = fixtures.createRestaurant(adminToken);
        Integer dishId = fixtures.createDish(adminToken, restaurantId, 50);
        ConcurrencyTestFixtures.RegisteredUser user = fixtures.registerUser();

        List<Callable<HttpStatusCode>> tasks = List.of(
                () -> fixtures.post("/api/v1/user-dishes", Map.of("dishId", dishId, "quantity", 1), user.accessToken())
                        .getStatusCode(),
                () -> fixtures.post("/api/v1/user-dishes", Map.of("dishId", dishId, "quantity", 1), user.accessToken())
                        .getStatusCode()
        );
        List<HttpStatusCode> results = ConcurrentRace.run(tasks, 20);
        assertThat(results).allMatch(HttpStatus.CREATED::equals, "cả hai request thêm giỏ hàng đều phải trả 201");

        List<Integer> quantities = jdbcTemplate.queryForList(
                "SELECT quantity FROM user_dish WHERE user_id = (SELECT id FROM user WHERE email = ?) AND dish_id = ?",
                Integer.class, user.email(), dishId);

        ResponseEntity<String> readCartAfter = fixtures.authGet("/api/v1/user-dishes", user.accessToken());

        assertAll(
                "Bất biến giỏ hàng: tối đa 1 dòng cho mỗi (user, dish), và đọc lại giỏ vẫn hoạt động",
                () -> assertThat(quantities).as("đúng 1 dòng, số lượng cộng dồn = 2").containsExactly(2),
                () -> assertThat(readCartAfter.getStatusCode())
                        .as("GET /user-dishes sau đó không được vỡ vì IncorrectResultSizeDataAccessException")
                        .isEqualTo(HttpStatus.OK)
        );
    }
}
