package com.example.foodie.catalog.dish.controller;

import com.example.foodie.catalog.dish.dto.response.DishDTO;
import com.example.foodie.catalog.tag.dto.response.TagResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

import static com.example.foodie.common.config.OpenApiConfig.BEARER_SECURITY_SCHEME;

@io.swagger.v3.oas.annotations.tags.Tag(name = "Dish", description = "Danh sách và thông tin món ăn")
public interface DishControllerDocs {

    @Operation(summary = "Lấy danh sách món ăn", description = "Trả về toàn bộ món ăn kèm nhà hàng, tag và đánh giá.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = DishDTO.class)))),
            @ApiResponse(responseCode = "404", description = "Không thể lấy danh sách món ăn")
    })
    @SecurityRequirement(name = BEARER_SECURITY_SCHEME)
    ResponseEntity<List<DishDTO>> getAllDishes();

    ResponseEntity<List<TagResponseDTO>> getTagsByDishId(@PathVariable Integer dishId);

    ResponseEntity<List<Float>> average_rating();

    ResponseEntity<List<Integer>> getAllDishId();
}
