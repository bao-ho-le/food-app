package com.example.foodie.common.idempotency.repository;

import com.example.foodie.common.idempotency.entity.IdempotencyKey;
import com.example.foodie.support.AbstractMySqlDataJpaTest;
import com.example.foodie.support.TestDataFixtures;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * IdempotencyKeyRepositoryTest.
 *
 * Item 1 (deleteByCreatedAtBefore) là derived query -- lẽ ra ngoài phạm vi Phase 4 (chỉ test
 * custom query), nhưng nằm trong bảng "Sản phẩm test" đóng của tài liệu vì đây là cơ chế dọn
 * key hết hạn (retention), hành vi nghiệp vụ quan trọng không có chỗ nào khác kiểm ở tầng
 * persistence.
 *
 * Item 2 không kiểm query nào cả -- nó xác nhận cột request_fingerprint (thêm ở migration V7,
 * VARCHAR(64)) ánh xạ đúng độ dài. Cột hụt độ dài sẽ âm thầm cắt cụt chuỗi SHA-256 hex 64 ký
 * tự và làm cơ chế phát hiện "cùng key khác nội dung" (IDEMPOTENCY_KEY_REQUEST_MISMATCH) sai
 * lệch mà không ném lỗi nào.
 */
class IdempotencyKeyRepositoryTest extends AbstractMySqlDataJpaTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private IdempotencyKeyRepository idempotencyKeyRepository;

    // created_at là @CreationTimestamp -- không set được qua entity, phải ghi đè bằng JPQL
    // UPDATE tường minh (updatable mặc định của @CreationTimestamp chỉ chặn auto dirty-checking,
    // không chặn JPQL) rồi clear() context. Cột này là MySQL TIMESTAMP (không phải DATETIME) --
    // tự quy đổi UTC theo session time_zone. Dùng JPQL thay vì native SQL để đường ghi và đường
    // đọc lại (qua deleteByCreatedAtBefore) đi cùng một cách Hibernate bind Instant; native SQL
    // với java.sql.Timestamp qua JDBC thô từng lệch múi giờ server, làm bản ghi "cũ" (2 giờ
    // trước) bị lưu thành mới hơn cutoff và không bị xoá.
    private void backdateCreatedAt(IdempotencyKey key, Instant when) {
        em.getEntityManager()
                .createQuery("UPDATE IdempotencyKey k SET k.createdAt = :when WHERE k.key = :key")
                .setParameter("when", when)
                .setParameter("key", key.getKey())
                .executeUpdate();
        em.clear();
    }

    // ---- item 1 ----

    @Test
    void should_deleteOnlyRecordsOlderThanCutoff() {
        Instant now = Instant.now();
        Instant cutoff = now;
        Instant old1CreatedAt = now.minus(2, ChronoUnit.HOURS);
        Instant old2CreatedAt = now.minus(1, ChronoUnit.HOURS);
        Instant freshCreatedAt = now.plus(1, ChronoUnit.HOURS);

        IdempotencyKey old1 = TestDataFixtures.idempotencyKey(em, 1, "f".repeat(64));
        backdateCreatedAt(old1, old1CreatedAt);
        IdempotencyKey old2 = TestDataFixtures.idempotencyKey(em, 1, "f".repeat(64));
        backdateCreatedAt(old2, old2CreatedAt);
        IdempotencyKey fresh = TestDataFixtures.idempotencyKey(em, 1, "f".repeat(64));
        backdateCreatedAt(fresh, freshCreatedAt);

        idempotencyKeyRepository.deleteByCreatedAtBefore(cutoff);
        // deleteByCreatedAtBefore là derived delete (không @Modifying/@Query): Spring Data
        // SELECT rồi gọi entityManager.remove() cho từng entity -- việc xoá chỉ là các thao
        // tác PENDING trong persistence context, chưa có DELETE nào chạy tới DB. clear() ngay
        // sau đó (không flush() trước) sẽ ĐÁNH RƠI các remove() đang chờ thay vì thực thi
        // chúng -- test sẽ xanh giả kể cả khi query đúng. Đây chính là cái bẫy "Modifying +
        // persistence context" tài liệu Phase 4 cảnh báo, áp dụng cho cả derived delete.
        em.flush();
        em.clear();

        assertThat(idempotencyKeyRepository.findById(old1.getKey())).isEmpty();
        assertThat(idempotencyKeyRepository.findById(old2.getKey())).isEmpty();
        assertThat(idempotencyKeyRepository.findById(fresh.getKey())).isPresent();
    }

    // ---- item 2 ----

    @Test
    void should_roundTripFullLengthFingerprint_when_savedAndReloaded() {
        String fingerprint = "a1b2c3d4e5f6".repeat(6).substring(0, 64);
        assertThat(fingerprint).hasSize(64);

        IdempotencyKey saved = TestDataFixtures.idempotencyKey(em, 1, fingerprint);
        em.clear();

        IdempotencyKey reloaded = idempotencyKeyRepository.findById(saved.getKey()).orElseThrow();
        assertThat(reloaded.getRequestFingerprint()).isEqualTo(fingerprint);
    }
}
