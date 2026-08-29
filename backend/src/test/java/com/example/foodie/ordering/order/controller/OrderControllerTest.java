package com.example.foodie.ordering.order.controller;

import com.example.foodie.ordering.order.dto.request.OrderDTO;
import com.example.foodie.ordering.order.dto.response.OrderResponseDTO;
import com.example.foodie.ordering.order.enums.Status;
import com.example.foodie.ordering.order.service.OrderIdempotencyService;
import com.example.foodie.ordering.order.service.OrderService;
import com.example.foodie.support.ControllerSliceTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Order từng bị lộ nguyên entity (kèm object User lồng bên trong, có cả password) qua response
 * body. OrderResponseDTO hiện đã tách riêng, các test 5/6/8 dưới đây assert trên JSON thô để giữ
 * hàng rào chống việc regression quay lại y hệt.
 */
@ControllerSliceTest(controllers = OrderController.class)
class OrderControllerTest {

    private static final String ORDERS = "/api/v1/orders";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private OrderIdempotencyService orderIdempotencyService;

    private static OrderResponseDTO sampleOrder() {
        return OrderResponseDTO.builder()
                .id(1)
                .userId(7)
                .status(Status.PENDING)
                .totalPrice(120000f)
                .deliveryAddress("123 Lê Lợi")
                .build();
    }

    // ---- item 1: thiếu addressId ----

    @Test
    void should_return400WithAddressIdInMessage_when_addressIdMissing() throws Exception {
        mockMvc.perform(post(ORDERS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("addressId")));
    }

    // ---- item 2 & 3: header Idempotency-Key tuỳ chọn, phải tới đúng service ----

    @Test
    void should_forwardIdempotencyKeyToService_when_headerPresent() throws Exception {
        when(orderIdempotencyService.createOrder(any(), any(), any()))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(sampleOrder()));

        OrderDTO body = new OrderDTO();
        body.setAddressId(3);

        mockMvc.perform(post(ORDERS)
                        .header("Idempotency-Key", "abc-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated());

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(orderIdempotencyService).createOrder(any(), any(), keyCaptor.capture());
        assertThat(keyCaptor.getValue()).isEqualTo("abc-123");
    }

    @Test
    void should_forwardNullIdempotencyKey_when_headerAbsent() throws Exception {
        when(orderIdempotencyService.createOrder(any(), any(), any()))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(sampleOrder()));

        OrderDTO body = new OrderDTO();
        body.setAddressId(3);

        mockMvc.perform(post(ORDERS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated());

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(orderIdempotencyService).createOrder(any(), any(), keyCaptor.capture());
        assertThat(keyCaptor.getValue()).isNull();
    }

    // ---- item 4, 5, 6: tạo đơn hợp lệ -> 201, body không rò rỉ user/password ----

    @Test
    void should_return201AndLeakFreeBody_when_orderCreatedSuccessfully() throws Exception {
        when(orderIdempotencyService.createOrder(any(), any(), any()))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(sampleOrder()));

        OrderDTO body = new OrderDTO();
        body.setAddressId(3);

        String rawResponse = mockMvc.perform(post(ORDERS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user").doesNotExist())
                .andExpect(jsonPath("$.userId").value(7))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.totalPrice").exists())
                .andExpect(jsonPath("$.deliveryAddress").exists())
                .andReturn().getResponse().getContentAsString();

        assertThat(rawResponse).doesNotContain("password");
    }

    // ---- item 7: huỷ đơn ----

    @Test
    void should_return200_when_cancelOrderSucceeds() throws Exception {
        when(orderService.cancelOwnOrder(any(), eq(5))).thenReturn(sampleOrder());

        mockMvc.perform(patch(ORDERS + "/user/5/cancel"))
                .andExpect(status().isOk());
    }

    // ---- item 8: danh sách đơn -> không phần tử nào chứa password / object user ----

    @Test
    void should_returnLeakFreeArray_when_listingOwnOrders() throws Exception {
        when(orderService.getAllOrdersByUserId(any()))
                .thenReturn(List.of(sampleOrder(), sampleOrder()));

        String rawResponse = mockMvc.perform(get(ORDERS + "/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].user").doesNotExist())
                .andExpect(jsonPath("$[1].user").doesNotExist())
                .andReturn().getResponse().getContentAsString();

        assertThat(rawResponse).doesNotContain("password");
    }

    // ---- item 9: sai kiểu path variable ----

    @Test
    void should_return400TypeMismatch_when_orderIdIsNotInteger() throws Exception {
        mockMvc.perform(get(ORDERS + "/user/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("TYPE_MISMATCH"));
    }
}
