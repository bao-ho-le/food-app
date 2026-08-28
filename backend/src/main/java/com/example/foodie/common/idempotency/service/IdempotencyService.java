package com.example.foodie.common.idempotency.service;

import com.example.foodie.common.exception.ErrorCode;
import com.example.foodie.common.exception.business_exception.CommonException;
import com.example.foodie.common.idempotency.entity.IdempotencyKey;
import com.example.foodie.common.idempotency.enums.IdempotencyStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.Supplier;


@Service
@RequiredArgsConstructor
public class IdempotencyService {
    private final IdempotencyKeyStore store;
    private final ObjectMapper objectMapper;

    public <T> ResponseEntity<T> execute(
            String idempotencyKey, String scope, Integer userId,
            Class<T> responseType, Supplier<ResponseEntity<T>> action
    ) {

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return action.get();
        }

        Optional<IdempotencyKey> reserved = store.tryReserve(idempotencyKey, scope, userId);
        if (reserved.isEmpty()) {
            return handleExisting(idempotencyKey, userId, responseType);
        }

        ResponseEntity<T> result;
        try {
            result = action.get();
        } catch (RuntimeException e) {
            store.release(idempotencyKey);
            throw e;
        }

        persistResponse(idempotencyKey, result);
        return result;
    }

    // Helper

    private <T> ResponseEntity<T> handleExisting(String idempotencyKey, Integer userId, Class<T> responseType) {
        IdempotencyKey existing = store.find(idempotencyKey)
                .orElseThrow(() -> new CommonException(ErrorCode.REQUEST_IN_PROGRESS));

        if (!existing.getUserId().equals(userId)) {
            throw new CommonException(ErrorCode.REQUEST_IN_PROGRESS);
        }
        if (existing.getStatus() == IdempotencyStatus.IN_PROGRESS) {
            throw new CommonException(ErrorCode.REQUEST_IN_PROGRESS);
        }
        return replay(existing, responseType);
    }

    private <T> void persistResponse(String idempotencyKey, ResponseEntity<T> result) {
        try {
            String json = objectMapper.writeValueAsString(result.getBody());
            store.markCompleted(idempotencyKey, result.getStatusCodeValue(), json);
        } catch (JsonProcessingException e) {
            throw new CommonException(ErrorCode.SOMETHING_WENT_WRONG, e);
        }
    }

    private <T> ResponseEntity<T> replay(IdempotencyKey existing, Class<T> responseType) {
        try {
            T body = objectMapper.readValue(existing.getResponseBody(), responseType);
            return ResponseEntity.status(existing.getResponseStatus()).body(body);
        } catch (JsonProcessingException e) {
            throw new CommonException(ErrorCode.SOMETHING_WENT_WRONG, e);
        }
    }
}