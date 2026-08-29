package com.example.foodie.support;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Phase 0: chứng minh migration Flyway (V1-V7) tự dựng được toàn bộ schema từ
 * một database MySQL rỗng, và schema đó khớp chính xác với entity.
 *
 * Oracle không phải "code làm gì" mà là một invariant hạ tầng: MySQL container
 * ở đây khởi động HOÀN TOÀN RỖNG (AbstractMySqlIntegrationTest không seed gì).
 * @SpringBootTest kế thừa chạy với spring.flyway.enabled=true và
 * spring.jpa.hibernate.ddl-auto=validate — nếu migration thiếu bảng/cột nào so
 * với entity, hoặc thừa ra thứ gì đó, context sẽ FAIL NGAY LÚC KHỞI ĐỘNG, tức
 * là cả class test này sẽ đỏ trước khi chạy tới bất kỳ @Test nào. Bản thân việc
 * class này load được context đã LÀ oracle chính của Phase 0.
 *
 * Trước khi có V1, việc này không thể chạy được: schema rỗng + V3 (ALTER TABLE
 * dish ...) sẽ báo lỗi "Table 'dish' doesn't exist" ngay từ bước migrate.
 */
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
        // Ràng buộc chk_dish_stock_quantity_non_negative (V3): CHECK (stock_quantity >= 0).
        // H2 không có CHECK này (schema H2 sinh từ entity, không qua Flyway), nên test
        // chống oversell ở H2 sẽ pass giả. Đây là bằng chứng ràng buộc thật sự tồn tại
        // trên MySQL và thực sự chặn giá trị âm.
        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO dish (name, price, is_available, stock_quantity) VALUES ('Pho', 50000, true, -1)"))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void should_reject_duplicate_refresh_token_jti() {
        // Ràng buộc uq_refresh_tokens_jti (V2): UNIQUE (jti).
        // Dùng role có sẵn thay vì tự insert: DataInitializer đã seed ADMIN/USER lúc
        // context khởi động (role.role_name là UNIQUE), tự insert lại sẽ đụng ràng buộc đó.
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
