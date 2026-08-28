package com.example.foodie.common.idempotency.service;

import com.example.foodie.common.idempotency.entity.IdempotencyKey;
import com.example.foodie.common.idempotency.enums.IdempotencyStatus;
import com.example.foodie.common.idempotency.repository.IdempotencyKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IdempotencyKeyStore {

    private final IdempotencyKeyRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<IdempotencyKey> tryReserve(String key, String scope, Integer userId) {

        if (repository.existsById(key)) return Optional.empty();

        try {

            IdempotencyKey record = IdempotencyKey.builder()
                    .key(key)
                    .userId(userId)
                    .scope(scope)
                    .status(IdempotencyStatus.IN_PROGRESS)
                    .build();

            return Optional.of(repository.saveAndFlush(record));

        } catch (DataIntegrityViolationException e) {
            return Optional.empty();
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markCompleted(String key, int status, String responseJson) {

        repository.findById(key).ifPresent(existing -> {
            existing.setStatus(IdempotencyStatus.COMPLETED);
            existing.setResponseStatus(status);
            existing.setResponseBody(responseJson);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void release(String key) {
        repository.deleteById(key);
    }

    public Optional<IdempotencyKey> find(String key) {
        return repository.findById(key);
    }
}
