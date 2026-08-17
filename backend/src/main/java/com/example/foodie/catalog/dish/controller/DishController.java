package com.example.foodie.catalog.dish.controller;

import com.example.foodie.catalog.dish.dto.response.DishDTO;
import com.example.foodie.catalog.dish.entity.Dish;
import com.example.foodie.catalog.tag.entity.Tag;
import com.example.foodie.catalog.dish.service.DishService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.foodie.catalog.dish.dto.request.DishRequestDTO;

import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import static com.example.foodie.common.config.OpenApiConfig.BEARER_SECURITY_SCHEME;


@RestController
@RequestMapping("${api.prefix}/dishes")
@AllArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "Dish", description = "Danh sách và thông tin món ăn")
public class DishController {

    private DishService dishService;

    @Operation(summary = "Lấy danh sách món ăn", description = "Trả về toàn bộ món ăn kèm nhà hàng, tag và đánh giá.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = DishDTO.class)))),
            @ApiResponse(responseCode = "404", description = "Không thể lấy danh sách món ăn")
    })
    @SecurityRequirement(name = BEARER_SECURITY_SCHEME)
    @GetMapping
    public ResponseEntity<List<DishDTO>> getAllDishes(){
        return ResponseEntity.ok(dishService.getAllDishes());
    }

    @GetMapping("/{dishId}/tags")
    public ResponseEntity<List<Tag>> getTagsByDishId(@PathVariable Integer dishId){
        return ResponseEntity.ok(dishService.getAllTags(dishId));
    }

    @PostMapping
    public ResponseEntity<Dish> createDish(@Valid @RequestBody DishRequestDTO dishRequestDTO){
        return ResponseEntity.ok(dishService.createDish(dishRequestDTO));
    }

    @GetMapping("/average_rating")
    public ResponseEntity<List<Float>> average_rating(){
        return ResponseEntity.ok(dishService.getAverageRatings());
    }

    @GetMapping("/allIds")
    public ResponseEntity<List<Integer>> getAllDishId(){
        return ResponseEntity.ok(dishService.getAllDishId());
    }

    @PostMapping("/blocking/{id}/{type}")
    public ResponseEntity<String> blocking(@PathVariable Integer id, @PathVariable Integer type){
        dishService.blocking(id, type);

        return ResponseEntity.ok("Success");
    }

}
