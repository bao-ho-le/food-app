package com.example.foodie.controllers;

import com.example.foodie.models.Restaurant;
import com.example.foodie.services.interfaces.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

import static com.example.foodie.config.OpenApiConfig.BEARER_SECURITY_SCHEME;



@RestController
@RequestMapping("${api.prefix}/restaurants")
@AllArgsConstructor
@Tag(name = "Restaurant", description = "Danh sách và quản lý nhà hàng")
@SecurityRequirement(name = BEARER_SECURITY_SCHEME)
public class RestaurantController {
    private RestaurantService restaurantService;

    @Operation(summary = "Lấy danh sách nhà hàng", description = "Trả về toàn bộ nhà hàng trong hệ thống.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Restaurant.class)))),
            @ApiResponse(responseCode = "404", description = "Không thể lấy danh sách nhà hàng")
    })
    @GetMapping
    public ResponseEntity<?> getAllRestaurants(){
        try{
            List<Restaurant> restaurants = restaurantService.getAllRestaurants();

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(restaurants);
        }catch(Exception e){
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createRestaurant(@Valid @RequestBody Restaurant restaurant){
        try {
            Restaurant newRestaurant = restaurantService.createRestaurant(restaurant);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(newRestaurant);
        }catch(Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRestaurant(@PathVariable Integer id, @RequestBody Restaurant restaurant) {
        try{
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(restaurantService.updateRestaurant(id, restaurant));
        }catch(Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }



    @PostMapping("/blocking/{id}/{type}")
    public ResponseEntity<?> blocking(@PathVariable Integer id, @PathVariable Integer type){
        try{
           restaurantService.blocking(id, type);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body("Success");
        }catch(Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }
    
}
