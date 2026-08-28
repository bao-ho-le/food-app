package com.example.foodie.auth.entity;

import com.example.foodie.identity.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "refresh_tokens",
        indexes = {
                @Index(name = "idx_refresh_tokens_user_id", columnList = "user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * JWT jti claim
     */
    @Column(unique = true, nullable = false, length = 36)
    private String jti;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    /**
     * Thời điểm hết hạn
     */
    @Column(nullable = false)
    private Instant expiresAt;


    private Instant revokedAt;

    /**
     * Thời điểm tạo
     */
    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;


}
