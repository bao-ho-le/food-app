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
import org.springframework.http.ResponseEntity;

import java.util.List;

import static com.example.foodie.common.config.OpenApiConfig.BEARER_SECURITY_SCHEME;

@Tag(name = "Restaurant", description = "Danh sách và quản lý nhà hàng")
@SecurityRequirement(name = BEARER_SECURITY_SCHEME)
public interface RestaurantControllerDocs {

    @Operation(summary = "Lấy danh sách nhà hàng", description = "Trả về toàn bộ nhà hàng trong hệ thống.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Restaurant.class))))
    })
    ResponseEntity<List<Restaurant>> getAllRestaurants();
}
