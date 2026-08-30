package com.example.foodie.identity.admin.dish.controller;

import com.example.foodie.catalog.dish.dto.request.DishRequestDTO;
import com.example.foodie.catalog.dish.dto.request.DishStockRequestDTO;
import com.example.foodie.catalog.dish.entity.Dish;
import com.example.foodie.catalog.dish.service.DishService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/admin/dishes")
@AllArgsConstructor
public class AdminDishController implements AdminDishControllerDocs {

    private final DishService dishService;

    @Override
    @PostMapping
    public ResponseEntity<Dish> createDish(@Valid @RequestBody DishRequestDTO dishRequestDTO){
        return ResponseEntity.ok(dishService.createDish(dishRequestDTO));
    }

    @Override
    @PutMapping("/{dishId}")
    public ResponseEntity<Dish> updateDish(@PathVariable Integer dishId, @Valid @RequestBody DishRequestDTO dishRequestDTO){
        return ResponseEntity.ok(dishService.updateDish(dishId, dishRequestDTO));
    }

    @Override
    @PostMapping("/blocking/{id}/{type}")
    public ResponseEntity<String> blocking(@PathVariable Integer id, @PathVariable Integer type){
        dishService.blocking(id, type);

        return ResponseEntity.ok("Success");
    }

    @Override
    @PostMapping("/{dishId}/stock")
    public ResponseEntity<Dish> restockDish(@PathVariable Integer dishId, @Valid @RequestBody DishStockRequestDTO dishStockRequestDTO){
        return ResponseEntity.ok(dishService.restockDish(dishId, dishStockRequestDTO));
    }
}
