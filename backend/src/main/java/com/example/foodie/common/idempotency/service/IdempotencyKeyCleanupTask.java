package com.example.foodie.common.idempotency.service;

import com.example.foodie.common.idempotency.repository.IdempotencyKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class IdempotencyKeyCleanupTask {
    private final IdempotencyKeyRepository repository;

    @Scheduled(cron = "0 0 3 * * *") // 3h sáng mỗi ngày
    @Transactional
    public void cleanup() {
        repository.deleteByCreatedAtBefore(Instant.now().minus(Duration.ofHours(48)));
    }
}
