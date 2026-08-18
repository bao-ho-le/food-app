package com.example.foodie.feedback.review.controller;

import com.example.foodie.feedback.review.dto.response.ReviewResponseDTO;
import com.example.foodie.feedback.review.entity.Review;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

import static com.example.foodie.common.config.OpenApiConfig.BEARER_SECURITY_SCHEME;

@Tag(name = "Review", description = "Đánh giá món ăn")
@SecurityRequirement(name = BEARER_SECURITY_SCHEME)
public interface ReviewControllerDocs {

    @Operation(summary = "Lấy đánh giá của một món ăn", description = "Trả về toàn bộ đánh giá của người dùng cho một món ăn cụ thể.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách đánh giá thành công",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ReviewResponseDTO.class)))),
            @ApiResponse(responseCode = "400", description = "Món ăn không tồn tại")
    })
    ResponseEntity<List<ReviewResponseDTO>> getAllReviewsByDishId(
            @Parameter(description = "ID của món ăn") @PathVariable(name="dish_id") Integer dishId);

    @Operation(summary = "Thêm đánh giá", description = "Thêm đánh giá cho một món ăn đã đặt (theo order_dish_id).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Thêm đánh giá thành công"),
            @ApiResponse(responseCode = "400", description = "Món ăn đã đặt không tồn tại hoặc dữ liệu không hợp lệ")
    })
    ResponseEntity<Review> addReview(
            @Parameter(description = "ID của món ăn trong đơn hàng") @PathVariable(name="order_dish_id") Integer orderDishId,
            @Valid @RequestBody Review review);
}
