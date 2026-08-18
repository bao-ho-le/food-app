package com.example.foodie.identity.admin.restaurant.controller;

import com.example.foodie.catalog.restaurant.entity.Restaurant;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import static com.example.foodie.common.config.OpenApiConfig.BEARER_SECURITY_SCHEME;

@Tag(name = "Admin - Restaurant", description = "Quản trị nhà hàng (chỉ ADMIN)")
@SecurityRequirement(name = BEARER_SECURITY_SCHEME)
public interface AdminRestaurantControllerDocs {

    ResponseEntity<Restaurant> createRestaurant(@Valid @RequestBody Restaurant restaurant);

    ResponseEntity<Restaurant> updateRestaurant(@PathVariable Integer id, @RequestBody Restaurant restaurant);

    ResponseEntity<String> blocking(@PathVariable Integer id, @PathVariable Integer type);
}
