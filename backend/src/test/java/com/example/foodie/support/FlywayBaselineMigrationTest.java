package com.example.foodie.support;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Phase 0: chứng minh migration Flyway (V1-V7) tự dựng được toàn bộ schema từ
// một database MySQL rỗng, và schema đó khớp chính xác với entity.
class FlywayBaselineMigrationTest extends AbstractMySqlIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void should_build_complete_schema_from_migrations_on_empty_database() {
        // Context đã load thành công ở bước @SpringBootTest (validate không lỗi).
        // Kiểm thêm rằng đúng 13 bảng baseline + 2 bảng do V2/V5 thêm đã tồn tại,
        // để bài test không chỉ dựa vào "không throw exception".
        List<String> tables = jdbcTemplate.queryForList(
                "SELECT LOWER(table_name) FROM information_schema.tables WHERE table_schema = DATABASE()",
                String.class);

        assertThat(tables).containsExactlyInAnyOrder(
                "role", "user", "address", "restaurant", "dish", "image", "category",
                "tag", "dish_tag", "user_dish", "orders", "order_dish", "review",
                "refresh_tokens", "idempotency_keys", "flyway_schema_history");
    }

    @Test
    void should_reject_negative_stock_quantity() {
        // Test trên MySQL và thực sự chặn giá trị âm.
        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO dish (name, price, is_available, stock_quantity) VALUES ('Pho', 50000, true, -1)"))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void should_reject_duplicate_refresh_token_jti() {
        // Test ràng buộc jti phải unique

        Integer roleId = jdbcTemplate.queryForObject("SELECT id FROM role LIMIT 1", Integer.class);

        jdbcTemplate.update(
                "INSERT INTO `user` (full_name, gender, phone_number, email, password, role_id, is_active) " +
                        "VALUES ('Jti Tester', 'MALE', '0900000001', 'jti-tester@test.local', 'x', ?, true)",
                roleId);

        Integer userId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);

        String sameJti = "11111111-1111-1111-1111-111111111111";
        jdbcTemplate.update(
                "INSERT INTO refresh_tokens (id, jti, user_id, expires_at, created_at) " +
                        "VALUES (UNHEX(REPLACE(UUID(), '-', '')), ?, ?, NOW(), NOW())",
                sameJti, userId);

        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO refresh_tokens (id, jti, user_id, expires_at, created_at) " +
                        "VALUES (UNHEX(REPLACE(UUID(), '-', '')), ?, ?, NOW(), NOW())",
                sameJti, userId))
                .isInstanceOf(DataAccessException.class);
    }
}
