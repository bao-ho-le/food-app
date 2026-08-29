package com.example.foodie.common.idempotency.service;

import com.example.foodie.common.exception.ErrorCode;
import com.example.foodie.common.exception.business_exception.CommonException;
import com.example.foodie.common.idempotency.entity.IdempotencyKey;
import com.example.foodie.common.idempotency.enums.IdempotencyStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Optional;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

// execute() bọc một thao tác nghiệp vụ để client gọi lại an toàn khi mạng chập chờn.
// IdempotencyKeyStore được mock (nằm trong danh sách "biên ngoài" cho phép mock theo
// Quy tắc 1). ObjectMapper dùng bản THẬT: fingerprint/replay là hành vi serialize/
// deserialize JSON thật, mock nó sẽ vô hiệu hoá chính thứ item 5/10/11 cần kiểm.
@ExtendWith(MockitoExtension.class)
class IdempotencyServiceTest {

    @Mock
    private IdempotencyKeyStore store;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private IdempotencyService idempotencyService;

    @SuppressWarnings("unchecked")
    private final Supplier<ResponseEntity<String>> action = mock(Supplier.class);

    @BeforeEach
    void setUp() {
        idempotencyService = new IdempotencyService(store, objectMapper);
    }

    private static IdempotencyKey existingKey(Integer userId, String fingerprint, IdempotencyStatus status) {
        return IdempotencyKey.builder()
                .key("some-key")
                .userId(userId)
                .scope("orders")
                .status(status)
                .requestFingerprint(fingerprint)
                .build();
    }

    @Nested
    class KhongCoKeyHoacKeyRong {

        // Idempotency-Key giờ là bắt buộc cho các thao tác đi qua execute() (hiện chỉ có tạo
        // đơn hàng) -- thiếu key không còn được coi là "khách hàng không cần bảo vệ", mà bị từ
        // chối thẳng để buộc client luôn gửi key, tránh race condition kiểu double-submit khi
        // key bị bỏ qua.
        @Test
        @DisplayName("key=null -> ném IDEMPOTENCY_KEY_REQUIRED, thao tác không chạy")
        void should_throwKeyRequired_when_keyIsNull() {
            assertThatThrownBy(() -> idempotencyService.execute(
                    null, "orders", 1, "payload", String.class, action))
                    .isInstanceOf(CommonException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.IDEMPOTENCY_KEY_REQUIRED);

            verifyNoInteractions(action, store);
        }

        @Test
        @DisplayName("key blank -> ném IDEMPOTENCY_KEY_REQUIRED, thao tác không chạy")
        void should_throwKeyRequired_when_keyIsBlank() {
            assertThatThrownBy(() -> idempotencyService.execute(
                    "   ", "orders", 1, "payload", String.class, action))
                    .isInstanceOf(CommonException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.IDEMPOTENCY_KEY_REQUIRED);

            verifyNoInteractions(action, store);
        }
    }

    @Nested
    class BienDoDaiKey {

        // 36 ký tự khớp kích thước cột khoá chính (idempotency_key VARCHAR(36)).
        private final String key36 = "a".repeat(36);
        private final String key37 = "a".repeat(37);

        @Test
        @DisplayName("key dài 37 ký tự ném IDEMPOTENCY_KEY_TOO_LONG, thao tác không chạy")
        void should_throwKeyTooLong_when_keyExceeds36Characters() {
            assertThatThrownBy(() -> idempotencyService.execute(
                    key37, "orders", 1, "payload", String.class, action))
                    .isInstanceOf(CommonException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.IDEMPOTENCY_KEY_TOO_LONG);

            verifyNoInteractions(action);
        }

        @Test
        @DisplayName("key dài đúng 36 ký tự được chấp nhận (biên trên hợp lệ)")
        void should_accept_when_keyIsExactly36Characters() {
            when(store.tryReserve(eq(key36), anyString(), any(), anyString()))
                    .thenReturn(Optional.of(IdempotencyKey.builder().key(key36).build()));
            when(action.get()).thenReturn(ResponseEntity.ok("done"));

            idempotencyService.execute(key36, "orders", 1, "payload", String.class, action);

            verify(action).get();
        }
    }

    @Nested
    class LanDauThucHien {

        @Test
        @DisplayName("key mới, thao tác trả 201+body -> response được lưu đúng status và JSON body")
        void should_persistResponseWithCorrectStatusAndBody_when_actionSucceeds() {
            when(store.tryReserve(eq("new-key"), eq("orders"), eq(1), anyString()))
                    .thenReturn(Optional.of(IdempotencyKey.builder().key("new-key").build()));
            when(action.get()).thenReturn(ResponseEntity.status(201).body("created-payload"));

            idempotencyService.execute("new-key", "orders", 1, "payload", String.class, action);

            ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
            verify(store).markCompleted(eq("new-key"), eq(201), jsonCaptor.capture());
            assertThat(jsonCaptor.getValue()).isEqualTo("\"created-payload\"");
        }

        // Không có test double nào "nhìn thấy" transaction; đây chỉ chứng minh key được
        // giải phóng ở tầng gọi store.release() và exception gốc được ném tiếp nguyên vẹn.
        @Test
        @DisplayName("thao tác ném RuntimeException -> key được giải phóng, exception ném tiếp nguyên vẹn")
        void should_releaseKeyAndRethrow_when_actionThrows() {
            when(store.tryReserve(eq("new-key"), anyString(), any(), anyString()))
                    .thenReturn(Optional.of(IdempotencyKey.builder().key("new-key").build()));
            RuntimeException boom = new RuntimeException("boom");
            when(action.get()).thenThrow(boom);

            assertThatThrownBy(() -> idempotencyService.execute(
                    "new-key", "orders", 1, "payload", String.class, action))
                    .isSameAs(boom);

            verify(store, times(1)).release("new-key");
            verify(store, never()).markCompleted(anyString(), any(Integer.class), anyString());
        }
    }

    @Nested
    class KeyDaTonTai {

        @Test
        @DisplayName("key đã tồn tại thuộc user khác -> REQUEST_IN_PROGRESS, không tiết lộ dữ liệu user kia")
        void should_throwRequestInProgress_when_existingKeyBelongsToAnotherUser() {
            when(store.tryReserve(eq("shared-key"), anyString(), eq(2), anyString()))
                    .thenReturn(Optional.empty());
            when(store.find("shared-key")).thenReturn(Optional.of(
                    existingKey(1, "irrelevant-fingerprint", IdempotencyStatus.COMPLETED)));

            assertThatThrownBy(() -> idempotencyService.execute(
                    "shared-key", "orders", 2, "payload", String.class, action))
                    .isInstanceOf(CommonException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.REQUEST_IN_PROGRESS);
        }

        @Test
        @DisplayName("key đã tồn tại, cùng user, khác fingerprint -> IDEMPOTENCY_KEY_REQUEST_MISMATCH")
        void should_throwRequestMismatch_when_sameUserButDifferentPayload() throws Exception {
            String storedFingerprint = sha256(objectMapper.writeValueAsString("old-payload"));
            when(store.tryReserve(eq("same-key"), anyString(), eq(1), anyString()))
                    .thenReturn(Optional.empty());
            when(store.find("same-key")).thenReturn(Optional.of(
                    existingKey(1, storedFingerprint, IdempotencyStatus.COMPLETED)));

            assertThatThrownBy(() -> idempotencyService.execute(
                    "same-key", "orders", 1, "different-payload", String.class, action))
                    .isInstanceOf(CommonException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.IDEMPOTENCY_KEY_REQUEST_MISMATCH);
        }

        @Test
        @DisplayName("key đã tồn tại, cùng user, cùng fingerprint, IN_PROGRESS -> REQUEST_IN_PROGRESS")
        void should_throwRequestInProgress_when_sameRequestStillInProgress() throws Exception {
            String fingerprint = sha256(objectMapper.writeValueAsString("payload"));
            when(store.tryReserve(eq("same-key"), anyString(), eq(1), anyString()))
                    .thenReturn(Optional.empty());
            when(store.find("same-key")).thenReturn(Optional.of(
                    existingKey(1, fingerprint, IdempotencyStatus.IN_PROGRESS)));

            assertThatThrownBy(() -> idempotencyService.execute(
                    "same-key", "orders", 1, "payload", String.class, action))
                    .isInstanceOf(CommonException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.REQUEST_IN_PROGRESS);
        }

        @Test
        @DisplayName("key đã tồn tại, cùng user, cùng fingerprint, COMPLETED -> phát lại đúng status/body, không chạy lại thao tác")
        void should_replayStoredResponse_when_sameRequestAlreadyCompleted() throws Exception {
            String fingerprint = sha256(objectMapper.writeValueAsString("payload"));
            IdempotencyKey completed = existingKey(1, fingerprint, IdempotencyStatus.COMPLETED);
            completed.setResponseStatus(200);
            completed.setResponseBody(objectMapper.writeValueAsString("cached-result"));

            when(store.tryReserve(eq("same-key"), anyString(), eq(1), anyString()))
                    .thenReturn(Optional.empty());
            when(store.find("same-key")).thenReturn(Optional.of(completed));

            ResponseEntity<String> result = idempotencyService.execute(
                    "same-key", "orders", 1, "payload", String.class, action);

            assertThat(result.getStatusCode().value()).isEqualTo(200);
            assertThat(result.getBody()).isEqualTo("cached-result");
            verifyNoInteractions(action);
        }
    }

    // Tính tất định của fingerprint là điều kiện tiên quyết để "cùng nội dung request"
    // có nghĩa -- nếu SHA-256(JSON) không ổn định giữa hai lần gọi, mọi item còn lại
    // (7-10) sụp đổ vì so sánh sai cả trường hợp giống lẫn khác nhau.
    @Test
    @DisplayName("Gọi execute hai lần với payload giống hệt nhau -> fingerprint gửi cho store giống nhau")
    void should_produceSameFingerprint_when_calledTwiceWithIdenticalPayload() {
        when(store.tryReserve(anyString(), anyString(), any(), anyString())).thenReturn(Optional.empty());
        when(store.find(anyString())).thenReturn(Optional.of(
                existingKey(1, "won't-match-anyway", IdempotencyStatus.IN_PROGRESS)));

        ArgumentCaptor<String> fingerprintCaptor = ArgumentCaptor.forClass(String.class);

        assertThatThrownBy(() -> idempotencyService.execute("key-1", "orders", 1, "same-payload", String.class, action))
                .isInstanceOf(CommonException.class);
        assertThatThrownBy(() -> idempotencyService.execute("key-2", "orders", 1, "same-payload", String.class, action))
                .isInstanceOf(CommonException.class);

        verify(store, times(2)).tryReserve(anyString(), anyString(), any(), fingerprintCaptor.capture());
        assertThat(fingerprintCaptor.getAllValues()).hasSize(2);
        assertThat(fingerprintCaptor.getAllValues().get(0)).isEqualTo(fingerprintCaptor.getAllValues().get(1));
    }

    private static String sha256(String json) throws Exception {
        byte[] hash = java.security.MessageDigest.getInstance("SHA-256")
                .digest(json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return java.util.HexFormat.of().formatHex(hash);
    }
}
