package com.example.foodie.feedback.review.controller;

import com.example.foodie.feedback.review.service.ReviewService;
import com.example.foodie.support.ControllerSliceTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Review.rating có @Min(1) @Max(5) trên chính entity (không phải DTO riêng) -- đây là tầng
 * đúng để kiểm miền giá trị này: Bean Validation chặn trước khi request chạm tới ReviewHelper
 * (nằm trong service đã mock), nên các nhánh kiểm rating trong helper không đạt tới được qua
 * HTTP -- Phase 1 đã cố tình bỏ qua chúng vì lý do này.
 */
@ControllerSliceTest(controllers = ReviewController.class)
class ReviewControllerTest {

    private static final String REVIEW_URL = "/api/v1/reviews/dish/1";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReviewService reviewService;

    @ParameterizedTest(name = "rating={0} -> {1}")
    @CsvSource({
            "0, 400",  // dưới biên dưới hợp lệ
            "1, 201",  // biên dưới hợp lệ
            "5, 201",  // biên trên hợp lệ
            "6, 400"   // trên biên trên hợp lệ
    })
    void should_enforceRatingBoundary_when_addingReview(int rating, int expectedStatus) throws Exception {
        var result = mockMvc.perform(post(REVIEW_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("rating", rating))))
                .andExpect(status().is(expectedStatus));

        if (expectedStatus == 400) {
            result.andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));
        }
    }
}
