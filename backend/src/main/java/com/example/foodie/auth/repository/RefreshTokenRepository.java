package com.example.foodie.auth.repository;

import com.example.foodie.auth.entity.RefreshToken;
import com.example.foodie.identity.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByJti(String jti);

    // BUG-001: gọi từ AuthServiceImpl.refresh() ngay trước một `throw` khi phát
    // hiện reuse. Với propagation mặc định (REQUIRED), UPDATE này tham gia
    // transaction của caller và bị rollback theo — vô hiệu hoá luôn việc thu
    // hồi toàn bộ token family. REQUIRES_NEW buộc nó commit độc lập, y hệt
    // cách IdempotencyKeyStore (cùng codebase) đã dùng cho vấn đề tương tự.
    @Modifying
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query("UPDATE RefreshToken r SET r.revokedAt = :now WHERE r.user = :user AND r.revokedAt IS NULL")
    int revokeAllByUser(@Param("user") User user, @Param("now") Instant now);

    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.expiresAt < :now")
    void deleteAllExpired(@Param("now") Instant now);

    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken r SET r.revokedAt = :now " +
            "WHERE r.jti = :jti AND r.revokedAt IS NULL")
    int revokeIfActive(@Param("jti") String jti, @Param("now") Instant now);
}