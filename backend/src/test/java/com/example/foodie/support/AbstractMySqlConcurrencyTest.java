package com.example.foodie.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Base cho test đồng thời (Phase 6) -- cần request HTTP thật chạy song song từ nhiều luồng,
 * điều MockMvc không hỗ trợ (xem javadoc AbstractMySqlIntegrationTest, Phase 5). Vì vậy
 * webEnvironment phải là RANDOM_PORT + TestRestTemplate thay vì MOCK + MockMvc -- đây là một
 * MergedContextConfiguration khác với AbstractMySqlIntegrationTest nên Spring không tái dùng
 * chung context được, nhưng cả 7 test class Phase 6 dùng chung đúng MỘT context mới này.
 *
 * Cùng container MySQL singleton (MySqlTestContainer) với Phase 0/4/5.
 *
 * spring.datasource.hikari.maximum-pool-size tăng lên vì mỗi luồng test giữ một connection
 * suốt transaction -- một số test bung tới 5 luồng cùng lúc, mặc định HikariCP (10) vẫn đủ
 * nhưng nới ra 20 để có biên an toàn khi nhiều test class nối tiếp nhau chưa kịp trả hết
 * connection cũ. Chỉ đổi qua property của riêng test, không đụng application.properties.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.flyway.enabled=true",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect",
        "spring.datasource.hikari.maximum-pool-size=20"
})
public abstract class AbstractMySqlConcurrencyTest implements MySqlTestContainer {

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected JdbcTemplate jdbcTemplate;
}
