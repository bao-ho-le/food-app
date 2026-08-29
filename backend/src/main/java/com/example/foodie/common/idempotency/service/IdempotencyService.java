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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;
import java.util.function.Supplier;


@Service
@RequiredArgsConstructor
public class IdempotencyService {
    private static final int MAX_KEY_LENGTH = 36;

    private final IdempotencyKeyStore store;
    private final ObjectMapper objectMapper;

    public <T> ResponseEntity<T> execute(
            String idempotencyKey, String scope, Integer userId, Object requestPayload,
            Class<T> responseType, Supplier<ResponseEntity<T>> action
    ) {

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return action.get();
        }
        if (idempotencyKey.length() > MAX_KEY_LENGTH) {
            throw new CommonException(ErrorCode.IDEMPOTENCY_KEY_TOO_LONG);
        }

        String fingerprint = fingerprint(requestPayload);

        Optional<IdempotencyKey> reserved = store.tryReserve(idempotencyKey, scope, userId, fingerprint);
        if (reserved.isEmpty()) {
            return handleExisting(idempotencyKey, userId, fingerprint, responseType);
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

    private <T> ResponseEntity<T> handleExisting(String idempotencyKey, Integer userId, String fingerprint, Class<T> responseType) {
        IdempotencyKey existing = store.find(idempotencyKey)
                .orElseThrow(() -> new CommonException(ErrorCode.REQUEST_IN_PROGRESS));

        if (!existing.getUserId().equals(userId)) {
            throw new CommonException(ErrorCode.REQUEST_IN_PROGRESS);
        }
        // Cùng key chỉ được replay khi request tương đương — khác nội dung thì từ
        // chối thẳng, bất kể key kia đang IN_PROGRESS hay đã COMPLETED.
        if (!existing.getRequestFingerprint().equals(fingerprint)) {
            throw new CommonException(ErrorCode.IDEMPOTENCY_KEY_REQUEST_MISMATCH);
        }
        if (existing.getStatus() == IdempotencyStatus.IN_PROGRESS) {
            throw new CommonException(ErrorCode.REQUEST_IN_PROGRESS);
        }
        return replay(existing, responseType);
    }

    private String fingerprint(Object requestPayload) {
        try {
            String json = objectMapper.writeValueAsString(requestPayload);
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(json.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (JsonProcessingException | NoSuchAlgorithmException e) {
            throw new CommonException(ErrorCode.SOMETHING_WENT_WRONG, e);
        }
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