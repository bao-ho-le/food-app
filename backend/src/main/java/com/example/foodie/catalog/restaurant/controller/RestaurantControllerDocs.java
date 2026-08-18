package com.example.foodie.catalog.restaurant.controller;

import com.example.foodie.catalog.restaurant.entity.Restaurant;
import io.swagger.v3.oas.annotations.Operation;
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

@Tag(name = "Restaurant", description = "Danh sách và quản lý nhà hàng")
@SecurityRequirement(name = BEARER_SECURITY_SCHEME)
public interface RestaurantControllerDocs {

    @Operation(summary = "Lấy danh sách nhà hàng", description = "Trả về toàn bộ nhà hàng trong hệ thống.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Restaurant.class)))),
            @ApiResponse(responseCode = "404", description = "Không thể lấy danh sách nhà hàng")
    })
    ResponseEntity<List<Restaurant>> getAllRestaurants();

    ResponseEntity<Restaurant> createRestaurant(@Valid @RequestBody Restaurant restaurant);

    ResponseEntity<Restaurant> updateRestaurant(@PathVariable Integer id, @RequestBody Restaurant restaurant);

    ResponseEntity<String> blocking(@PathVariable Integer id, @PathVariable Integer type);
}
