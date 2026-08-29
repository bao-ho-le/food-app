package com.example.foodie.support;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;

/**
 * Container MySQL dùng chung cho mọi test cần DB thật -- cả @SpringBootTest
 * (AbstractMySqlIntegrationTest, Phase 0) lẫn @DataJpaTest (AbstractMySqlDataJpaTest, Phase 4).
 *
 * CỐ TÌNH không dùng @Testcontainers/@Container: extension JUnit5 của Testcontainers chỉ chia
 * sẻ container static TRONG PHẠM VI MỘT test class -- nó start() ở beforeAll và stop() ở
 * afterAll của TỪNG class riêng biệt. Với 8+ test class Phase 4 cùng implement interface này,
 * điều đó nghĩa là container bị dừng rồi khởi động lại (container Docker MỚI, port MỚI) ở
 * ranh giới mỗi class -- đã kiểm chứng thực tế: log cho thấy "Creating container for image"
 * lặp lại nhiều lần với port khác nhau, cuối cùng làm cạn kết nối/tài nguyên Docker và test
 * timeout với HikariPool "total=0".
 *
 * Cách đúng (theo tài liệu Testcontainers, "singleton container" pattern): tự start() container
 * một lần trong static initializer, không giao cho @Testcontainers extension quản lý. @ServiceConnection
 * vẫn hoạt động bình thường trên field không có @Container -- Spring Boot hỗ trợ việc này, chỉ
 * là lúc đó bạn tự chịu trách nhiệm start/stop. Container không cần stop() chủ động: Ryuk (tự
 * kích hoạt bởi Testcontainers core ngay khi container đầu tiên được tạo, không phụ thuộc
 * @Testcontainers) dọn nó khi JVM thoát.
 */
public interface MySqlTestContainer {

    // Field trong interface được nạp (và initializer -- gồm cả start() bên trong
    // newStartedContainer() -- chạy) đúng một lần cho cả JVM, bất kể bao nhiêu test class
    // implement interface này.
    @ServiceConnection
    MySQLContainer<?> MYSQL = newStartedContainer();

    private static MySQLContainer<?> newStartedContainer() {
        MySQLContainer<?> container = new MySQLContainer<>("mysql:8.4");
        container.start();
        return container;
    }
}
