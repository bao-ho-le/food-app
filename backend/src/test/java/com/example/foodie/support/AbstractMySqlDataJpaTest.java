package com.example.foodie.support;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

/**
 * Base cho các test tầng persistence (Phase 4) -- kiểm custom @Query/@Modifying trên schema
 * MySQL thật, không phải H2.
 *
 * @DataJpaTest mặc định thay DataSource bằng DB in-memory (embedded); Replace.NONE tắt hành
 * vi đó để giữ lại kết nối tới container MySQL do @ServiceConnection cấu hình. Cùng lý do như
 * AbstractMySqlIntegrationTest (Phase 0): schema phải đến từ Flyway thật (V1-V7), không phải
 * ddl-auto=create-drop sinh từ entity, vì migration mang các ràng buộc (CHECK, UNIQUE...)
 * entity không khai báo.
 *
 * Implement chung MySqlTestContainer với AbstractMySqlIntegrationTest -- field container là
 * static trong interface nên chỉ được nạp/khởi động một lần cho toàn bộ JVM chạy test, dù
 * @SpringBootTest (Phase 0) và @DataJpaTest (Phase 4) là hai loại context hoàn toàn khác nhau.
 */
@DataJpaTest(properties = {
        "spring.flyway.enabled=true",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class AbstractMySqlDataJpaTest implements MySqlTestContainer {
}
