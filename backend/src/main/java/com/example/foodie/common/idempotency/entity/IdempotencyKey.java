package com.example.foodie.common.idempotency.entity;

import com.example.foodie.common.idempotency.enums.IdempotencyStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "idempotency_keys",
        indexes = {
                @Index(name = "idx_idempotency_user_id", columnList = "user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdempotencyKey {

    @Id
    @Column(name = "idempotency_key", length = 36)
    private String key;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(nullable = false, length = 100)
    private String scope;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IdempotencyStatus status;

    private Integer responseStatus;

    @Lob
    private String responseBody;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

}