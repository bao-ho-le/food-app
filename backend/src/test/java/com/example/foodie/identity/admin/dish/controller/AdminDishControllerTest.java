package com.example.foodie.identity.admin.dish.controller;

import com.example.foodie.catalog.dish.service.DishService;
import com.example.foodie.support.ControllerSliceTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Chỉ kiểm phần Bean Validation của DishRequestDTO/DishStockRequestDTO chặn được ở tầng MVC.
 * Các ràng buộc còn lại (tên rỗng chuỗi, tên > 255 ký tự, giá âm, tags null) nằm trong
 * DishHelper -- bên trong DishService đã mock -- nên không kiểm được ở Phase 3, chuyển sang
 * Phase 5.
 */
@ControllerSliceTest(controllers = AdminDishController.class)
class AdminDishControllerTest {

    private static final String DISHES = "/api/v1/admin/dishes";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DishService dishService;

    // ---- item 1: thiếu name ----

    @Test
    void should_return400ValidationFailed_when_nameMissing() throws Exception {
        mockMvc.perform(post(DISHES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("price", 10000, "restaurantId", 1))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));
    }

    // ---- item 2: thiếu restaurantId ----

    @Test
    void should_return400ValidationFailed_when_restaurantIdMissing() throws Exception {
        mockMvc.perform(post(DISHES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", "Phở bò", "price", 10000))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));
    }

    // ---- item 3: POST stock thiếu quantity ----

    @Test
    void should_return400ValidationFailed_when_stockQuantityMissing() throws Exception {
        mockMvc.perform(post(DISHES + "/1/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));
    }
}
