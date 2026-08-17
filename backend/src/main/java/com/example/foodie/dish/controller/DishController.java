package com.example.foodie.dish.controller;

import com.example.foodie.dish.dto.response.DishDTO;
import com.example.foodie.dish.entity.Dish;
import com.example.foodie.tag.entity.Tag;
import com.example.foodie.dish.service.DishService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.example.foodie.dish.dto.request.DishRequestDTO;

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
    public ResponseEntity<?> getAllDishes(){

        try {
            List<DishDTO> dishes = dishService.getAllDishes();

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(dishes);
        }catch (Exception e){
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    @GetMapping("/{dishId}/tags")
    public ResponseEntity<?> getTagsByDishId(@PathVariable Integer dishId){
        try {
            List<Tag> tags = dishService.getAllTags(dishId); // gọi service để lấy tag

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(tags);
        } catch (Exception e){
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createDish(@Valid @RequestBody DishRequestDTO dishRequestDTO){

        try{
            Dish newDish = dishService.createDish(dishRequestDTO);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(newDish);
        }catch (Exception e){
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @GetMapping("/average_rating")
    public ResponseEntity<?> average_rating(){

        try{
            List<Float> dishes_rating = dishService.getAverageRatings();

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(dishes_rating);
        }catch (Exception e){
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @GetMapping("/allIds")
    public ResponseEntity<?> getAllDishId(){

        try{
            List<Integer> dallDishId = dishService.getAllDishId();

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(dallDishId);
        }catch (Exception e){
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/blocking/{id}/{type}")
    public ResponseEntity<?> blocking(@PathVariable Integer id, @PathVariable Integer type){
        try{
           dishService.blocking(id, type);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body("Success");
        }catch (Exception e){
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
        
    }
    
}
