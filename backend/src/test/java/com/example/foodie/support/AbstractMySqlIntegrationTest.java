package com.example.foodie.support;

import org.springframework.boot.test.context.SpringBootTest;

/**
 * Base cho các test cần MySQL thật (không phải H2), vì hai lý do độc lập:
 * 1. H2 không mô phỏng khoá hàng SELECT ... FOR UPDATE của InnoDB.
 * 2. Schema H2 hiện sinh từ entity (ddl-auto=create-drop) nên thiếu các ràng buộc
 *    CHECK/UNIQUE chỉ tồn tại trong migration Flyway thật (V2, V3, ...).
 *
 * Container (MySqlTestContainer) dùng chung với AbstractMySqlDataJpaTest (Phase 4) -- xem
 * javadoc ở đó để biết vì sao chỉ một container được khởi động cho cả build.
 */
@SpringBootTest(properties = {
        "spring.flyway.enabled=true",
        "spring.jpa.hibernate.ddl-auto=validate",
        // src/test/resources/application.properties ép dialect=H2Dialect cho mọi test;
        // ở đây phải trả lại đúng dialect MySQL, nếu không Hibernate tự ý dò schema theo
        // kiểu H2 (vd. query INFORMATION_SCHEMA.SEQUENCES chỉ tồn tại ở H2) trên kết nối MySQL thật.
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect"
})
public abstract class AbstractMySqlIntegrationTest implements MySqlTestContainer {
}
