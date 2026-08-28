package com.example.foodie.identity.admin.dish.controller;

import com.example.foodie.catalog.dish.dto.request.DishRequestDTO;
import com.example.foodie.catalog.dish.dto.request.DishStockRequestDTO;
import com.example.foodie.catalog.dish.entity.Dish;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import static com.example.foodie.common.config.OpenApiConfig.BEARER_SECURITY_SCHEME;

@Tag(name = "Admin - Dish", description = "Quản trị món ăn (chỉ ADMIN)")
@SecurityRequirement(name = BEARER_SECURITY_SCHEME)
public interface AdminDishControllerDocs {

    ResponseEntity<Dish> createDish(@Valid @RequestBody DishRequestDTO dishRequestDTO);

    ResponseEntity<String> blocking(@PathVariable Integer id, @PathVariable Integer type);

    ResponseEntity<Dish> restockDish(@PathVariable Integer dishId, @Valid @RequestBody DishStockRequestDTO dishStockRequestDTO);
}
