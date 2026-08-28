package com.example.foodie.common.idempotency.repository;

import com.example.foodie.common.idempotency.entity.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, String> {
    void deleteByCreatedAtBefore(Instant cutoff);
}