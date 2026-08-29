package com.example.foodie;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Rẻ nhất trong cả test suite nhưng bắt được nhiều lỗi nhất: mọi placeholder
 * @Value thiếu default, mọi bean không wire được, mọi @Query JPQL sai cú pháp
 * sẽ làm test này đỏ ngay khi context khởi động.
 *
 * Trước khi thêm cookie.secure vào application-test.properties (BUG-011),
 * test này (và MỌI @SpringBootTest khác) fail với:
 *   Could not resolve placeholder 'cookie.secure' in value "${cookie.secure}"
 */
@SpringBootTest
class AppContextLoadsTest {

    @Test
    void contextLoads() {
        // Không cần assertion nào — nếu context load được, test này pass.
    }
}
