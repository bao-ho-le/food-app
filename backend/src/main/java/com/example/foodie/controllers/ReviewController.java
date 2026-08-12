package com.example.foodie.controllers;

import com.example.foodie.dtos.ReviewResponseDTO;
import com.example.foodie.models.Address;
import com.example.foodie.models.Restaurant;
import com.example.foodie.models.Review;
import com.example.foodie.services.interfaces.ReviewService;
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
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.foodie.config.OpenApiConfig.BEARER_SECURITY_SCHEME;

@RestController
@RequestMapping("${api.prefix}/reviews")
@AllArgsConstructor
@Tag(name = "Review", description = "Đánh giá món ăn")
@SecurityRequirement(name = BEARER_SECURITY_SCHEME)
public class ReviewController {
    private ReviewService reviewService;

    @Operation(summary = "Lấy đánh giá của một món ăn", description = "Trả về toàn bộ đánh giá của người dùng cho một món ăn cụ thể.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách đánh giá thành công",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ReviewResponseDTO.class)))),
            @ApiResponse(responseCode = "400", description = "Món ăn không tồn tại")
    })
    @GetMapping("/dish/{dish_id}")
    public ResponseEntity<?> getAllReviewsByDishId(
            @Parameter(description = "ID của món ăn") @PathVariable(name="dish_id") Integer dishId){
        List<ReviewResponseDTO> reviews = reviewService.findAllReviewsByDishId(dishId);

        try{
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(reviews);

        } catch(RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @Operation(summary = "Thêm đánh giá", description = "Thêm đánh giá cho một món ăn đã đặt (theo order_dish_id).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Thêm đánh giá thành công"),
            @ApiResponse(responseCode = "400", description = "Món ăn đã đặt không tồn tại hoặc dữ liệu không hợp lệ")
    })
    @PostMapping("/dish/{order_dish_id}")
    public ResponseEntity<?> addReview(
            @Parameter(description = "ID của món ăn trong đơn hàng") @PathVariable(name="order_dish_id") Integer orderDishId,
            @Valid @RequestBody Review review){
        try {
            Review newReview = reviewService.addReview(orderDishId, review);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(newReview);
        }catch(Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }
}
