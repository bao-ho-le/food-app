package com.example.foodie.ordering.userdish.controller;

import com.example.foodie.ordering.userdish.service.UserDishService;
import com.example.foodie.support.ControllerSliceTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * BVA quanh @Min(1) của UserDishDTO.quantity trên đường thêm mới. Lưu ý: khi CẬP NHẬT giỏ
 * hàng, quantity <= 0 lại hợp lệ (tín hiệu xoá món) -- bất đối xứng này đã kiểm ở Phase 1/2,
 * ở đây chỉ kiểm ràng buộc của POST (thêm mới).
 */
@ControllerSliceTest(controllers = UserDishController.class)
class UserDishControllerTest {

    private static final String USER_DISHES = "/api/v1/user-dishes";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserDishService userDishService;

    // ---- item 1: thiếu dishId ----

    @Test
    void should_return400ValidationFailed_when_dishIdMissing() throws Exception {
        mockMvc.perform(post(USER_DISHES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("quantity", 2))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));
    }

    // ---- item 2: quantity = 0 -> 400 (biên dưới không hợp lệ) ----

    @Test
    void should_return400ValidationFailed_when_quantityIsZero() throws Exception {
        mockMvc.perform(post(USER_DISHES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("dishId", 1, "quantity", 0))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));
    }

    // ---- item 3: quantity = 1 -> 201 (biên dưới hợp lệ) ----

    @Test
    void should_return201_when_quantityIsOne() throws Exception {
        mockMvc.perform(post(USER_DISHES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("dishId", 1, "quantity", 1))))
                .andExpect(status().isCreated());
    }

    // ---- item 4: PUT thiếu userDishId ----

    @Test
    void should_return400ValidationFailed_when_userDishIdMissingOnUpdate() throws Exception {
        mockMvc.perform(put(USER_DISHES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("quantity", 3))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));
    }
}
