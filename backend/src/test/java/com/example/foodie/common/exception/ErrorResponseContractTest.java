package com.example.foodie.common.exception;

import com.example.foodie.common.exception.business_exception.BusinessException;
import com.example.foodie.ordering.order.controller.OrderController;
import com.example.foodie.ordering.order.service.OrderIdempotencyService;
import com.example.foodie.ordering.order.service.OrderService;
import com.example.foodie.support.ControllerSliceTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Kiểm hợp đồng chung của mọi lỗi nghiệp vụ: GlobalExceptionHandler phải luôn trả cùng một
 * hình dạng body (timestamp/status/error/message/path) và mỗi ErrorCode phải ánh xạ đúng một
 * HTTP status. Frontend bind theo field "error" (= ErrorCode.name()), nên sai tên mã lỗi là
 * defect thật dù status đúng.
 *
 * Dùng chung một endpoint (GET /orders/user/{order_id}, service OrderService bị mock) để bắn
 * mọi ErrorCode -- handler xử lý generic theo BusinessException, không quan tâm exception ném
 * từ controller/service nào, nên không cần dựng riêng cho mỗi domain.
 */
@ControllerSliceTest(controllers = OrderController.class)
class ErrorResponseContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private OrderIdempotencyService orderIdempotencyService;

    private static final String URL = "/api/v1/orders/user/9";

    // ---- Hình dạng body (2 item) ----

    @Test
    void should_returnAllFiveFields_when_businessExceptionThrown() throws Exception {
        when(orderService.getOwnOrderItems(any(), eq(9)))
                .thenThrow(new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        mockMvc.perform(get(URL))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("ORDER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(ErrorCode.ORDER_NOT_FOUND.getMessage()))
                .andExpect(jsonPath("$.path").value("/api/v1/orders/user/9"));
    }

    @Test
    void should_matchResponseStatus_when_statusFieldCompared() throws Exception {
        when(orderService.getOwnOrderItems(any(), eq(9)))
                .thenThrow(new BusinessException(ErrorCode.ORDER_NOT_OWNER));

        mockMvc.perform(get(URL))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    // ---- Ma trận ErrorCode -> HTTP status (12 item) ----

    static Stream<Arguments> errorCodeToStatus() {
        return Stream.of(
                Arguments.of(ErrorCode.ORDER_NOT_FOUND, HttpStatus.NOT_FOUND),
                Arguments.of(ErrorCode.ORDER_NOT_OWNER, HttpStatus.FORBIDDEN),
                Arguments.of(ErrorCode.ORDER_INVALID_STATUS_TRANSITION, HttpStatus.CONFLICT),
                Arguments.of(ErrorCode.ORDER_STATUS_CONFLICT, HttpStatus.CONFLICT),
                Arguments.of(ErrorCode.ORDER_DELETE_FORBIDDEN, HttpStatus.CONFLICT),
                Arguments.of(ErrorCode.ADDRESS_NOT_FOUND, HttpStatus.NOT_FOUND),
                Arguments.of(ErrorCode.DISH_NOT_FOUND, HttpStatus.NOT_FOUND),
                Arguments.of(ErrorCode.DISH_OUT_OF_STOCK, HttpStatus.CONFLICT),
                Arguments.of(ErrorCode.DISH_NOT_AVAILABLE, HttpStatus.CONFLICT),
                Arguments.of(ErrorCode.USER_EMAIL_ALREADY_EXISTS, HttpStatus.CONFLICT),
                Arguments.of(ErrorCode.IDEMPOTENCY_KEY_TOO_LONG, HttpStatus.BAD_REQUEST),
                Arguments.of(ErrorCode.IDEMPOTENCY_KEY_REQUEST_MISMATCH, HttpStatus.UNPROCESSABLE_ENTITY)
        );
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @MethodSource("errorCodeToStatus")
    void should_mapErrorCodeToDeclaredHttpStatus_when_businessExceptionThrown(
            ErrorCode errorCode, HttpStatus expectedStatus) throws Exception {
        when(orderService.getOwnOrderItems(any(), eq(9)))
                .thenThrow(new BusinessException(errorCode));

        mockMvc.perform(get(URL))
                .andExpect(status().is(expectedStatus.value()))
                .andExpect(jsonPath("$.error").value(errorCode.name()));
    }

    // ---- Sai kiểu tham số path variable (1 item) ----

    @Test
    void should_return400WithTypeMismatch_when_pathVariableIsNotInteger() throws Exception {
        mockMvc.perform(get("/api/v1/orders/user/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("TYPE_MISMATCH"));
    }
}
