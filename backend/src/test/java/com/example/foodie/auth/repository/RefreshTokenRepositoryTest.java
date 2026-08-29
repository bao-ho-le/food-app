package com.example.foodie.auth.repository;

import com.example.foodie.auth.entity.RefreshToken;
import com.example.foodie.identity.user.entity.Role;
import com.example.foodie.identity.user.entity.User;
import com.example.foodie.identity.user.enums.RoleName;
import com.example.foodie.support.AbstractMySqlDataJpaTest;
import com.example.foodie.support.TestDataFixtures;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.transaction.TestTransaction;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * revokeIfActive là cơ chế phát hiện refresh-token bị dùng lại (INV-11, xem
 * RefreshTokenReuseDetectionTest): giá trị trả về (số dòng bị đổi) LÀ tín hiệu "token còn
 * hiệu lực hay không", không chỉ là side-effect. Nếu WHERE clause mất điều kiện
 * "revokedAt IS NULL", item 2 dưới đây sẽ âm thầm trả 1 thay vì 0 và cơ chế phát hiện reuse
 * sụp đổ mà không có dấu hiệu nào khác.
 */
class RefreshTokenRepositoryTest extends AbstractMySqlDataJpaTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private static final Instant FUTURE = Instant.now().plus(30, ChronoUnit.DAYS);

    private User newUser() {
        Role role = TestDataFixtures.role(em, RoleName.USER);
        return TestDataFixtures.user(em, role);
    }

    // ---- item 1 + 2 + 3: revokeIfActive ----

    @Test
    void should_revokeAndReturnOne_when_tokenIsActive() {
        User user = newUser();
        RefreshToken token = TestDataFixtures.refreshToken(em, user, FUTURE);

        // Commit dữ liệu fixture để transaction REQUIRES_NEW nhìn thấy token
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
        int updated = refreshTokenRepository.revokeIfActive(token.getJti(), now);

        assertThat(updated).isEqualTo(1);

        // @Modifying chạy thẳng xuống DB, không cập nhật entity đang nằm trong persistence
        // context -- phải clear() rồi đọc lại mới thấy giá trị thật.
        em.clear();
        RefreshToken reloaded = refreshTokenRepository.findByJti(token.getJti()).orElseThrow();
        assertThat(reloaded.getRevokedAt()).isEqualTo(now);
    }

    @Test
    void should_returnZero_when_revokingAlreadyRevokedToken() {
        User user = newUser();
        RefreshToken token = TestDataFixtures.refreshToken(em, user, FUTURE);

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        refreshTokenRepository.revokeIfActive(token.getJti(), Instant.now());
        em.clear();

        // Lần thu hồi thứ hai trên cùng jti: token đã revoked thì không revoke lại được.
        int secondAttempt = refreshTokenRepository.revokeIfActive(token.getJti(), Instant.now());
        assertThat(secondAttempt).isEqualTo(0);
    }

    @Test
    void should_returnZero_when_jtiDoesNotExist() {
        int updated = refreshTokenRepository.revokeIfActive("khong-ton-tai", Instant.now());
        assertThat(updated).isEqualTo(0);
    }

    // ---- item 4 + 6: revokeAllByUser ----

    @Test
    void should_revokeOnlyStillActiveTokens_andKeepAlreadyRevokedOnesUnchanged() {
        User user = newUser();
        RefreshToken t1 = TestDataFixtures.refreshToken(em, user, FUTURE);
        RefreshToken t2 = TestDataFixtures.refreshToken(em, user, FUTURE);
        RefreshToken t3 = TestDataFixtures.refreshToken(em, user, FUTURE);

        // Commit toàn bộ fixture
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        refreshTokenRepository.revokeIfActive(t3.getJti(), Instant.now());
        em.clear();

        Instant t3RevokedAtBefore = refreshTokenRepository.findByJti(t3.getJti()).orElseThrow().getRevokedAt();

        // revokeAllByUser dùng @Transactional(REQUIRES_NEW) (xem comment BUG-001 trong
        // RefreshTokenRepository) -- nó chạy trên một transaction/connection RIÊNG, không
        // thấy được dữ liệu chưa commit của transaction test hiện tại (@DataJpaTest mặc định
        // rollback cuối test, nên mọi thứ ở trên vẫn "chưa commit" theo góc nhìn của transaction
        // khác). Phải commit thật rồi mở lại transaction mới, nếu không revokeAllByUser sẽ
        // không tìm thấy dòng nào và trả về 0 dù JPQL đúng.
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        int revoked = refreshTokenRepository.revokeAllByUser(user, Instant.now());
        assertThat(revoked).isEqualTo(2);

        em.clear();
        assertThat(refreshTokenRepository.findByJti(t1.getJti()).orElseThrow().getRevokedAt()).isNotNull();
        assertThat(refreshTokenRepository.findByJti(t2.getJti()).orElseThrow().getRevokedAt()).isNotNull();
        // Token đã thu hồi từ trước phải giữ nguyên revokedAt cũ, không bị ghi đè bởi lần gọi này.
        assertThat(refreshTokenRepository.findByJti(t3.getJti()).orElseThrow().getRevokedAt())
                .isEqualTo(t3RevokedAtBefore);
    }

    // ---- item 5: không đụng token của user khác ----

    @Test
    void should_notTouchOtherUsersTokens_when_revokingAllByUser() {
        User userA = newUser();
        User userB = newUser();
        RefreshToken tokenA = TestDataFixtures.refreshToken(em, userA, FUTURE);
        RefreshToken tokenB = TestDataFixtures.refreshToken(em, userB, FUTURE);

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        refreshTokenRepository.revokeAllByUser(userA, Instant.now());

        em.clear();
        assertThat(refreshTokenRepository.findByJti(tokenA.getJti()).orElseThrow().getRevokedAt()).isNotNull();
        assertThat(refreshTokenRepository.findByJti(tokenB.getJti()).orElseThrow().getRevokedAt()).isNull();
    }
}
