package com.example.foodie.identity.address;

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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ⚠️ DỰ KIẾN ĐỎ -- thí nghiệm xác nhận, đã đọc code xác nhận nguyên nhân nhưng CHƯA sửa.
 *
 * Đặc tả: mỗi user có tối đa MỘT địa chỉ mặc định tại mọi thời điểm.
 *
 * Nguyên nhân đã xác nhận bằng đọc code: AddressServiceImpl không có @Transactional;
 * updateAddress làm hai việc rời nhau -- unsetCurrentDefaultAddress (đọc địa chỉ đang mặc
 * định, set false, save) rồi mới set địa chỉ mới thành true, save. Hai request đồng thời đặt
 * isDefault=true cho hai địa chỉ KHÁC NHAU đều đọc cùng một "địa chỉ mặc định hiện tại" trước
 * khi cái nào commit xong -- có thể cả hai đều tắt cùng một địa chỉ cũ rồi cả hai đều bật địa
 * chỉ của mình lên (kết quả: 2 địa chỉ mặc định), hoặc tuỳ interleaving khác mà còn 0.
 */
class DefaultAddressConcurrencyTest extends AbstractMySqlConcurrencyTest {

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
    void should_haveExactlyOneDefault_when_twoAddressesSetDefaultConcurrently() throws Exception {
        ConcurrencyTestFixtures.RegisteredUser user = fixtures.registerUser();
        String line1 = "Default Race Street 1";
        String line2 = "Default Race Street 2";
        Integer address1 = fixtures.createAddress(user.accessToken(), line1, true);
        Integer address2 = fixtures.createAddress(user.accessToken(), line2, false);

        List<Callable<HttpStatusCode>> tasks = List.of(
                () -> fixtures.exchange("/api/v1/address/user/" + address1, HttpMethod.PUT,
                                Map.of("address", line1, "isDefault", true), user.accessToken())
                        .getStatusCode(),
                () -> fixtures.exchange("/api/v1/address/user/" + address2, HttpMethod.PUT,
                                Map.of("address", line2, "isDefault", true), user.accessToken())
                        .getStatusCode()
        );

        List<HttpStatusCode> results = ConcurrentRace.run(tasks, 20);
        assertThat(results).allMatch(HttpStatus.OK::equals, "cả hai request cập nhật địa chỉ đều phải trả 200");

        Integer defaultCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM address WHERE user_id = (SELECT id FROM user WHERE email = ?) AND is_default = 1",
                Integer.class, user.email());
        assertThat(defaultCount).as("đúng 1 địa chỉ mặc định sau khi cả hai request hoàn tất").isEqualTo(1);
    }
}
