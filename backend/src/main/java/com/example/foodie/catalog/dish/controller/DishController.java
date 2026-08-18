package com.example.foodie.catalog.dish.controller;

import com.example.foodie.catalog.dish.dto.response.DishDTO;
import com.example.foodie.catalog.dish.entity.Dish;
import com.example.foodie.catalog.tag.entity.Tag;
import com.example.foodie.catalog.dish.service.DishService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.foodie.catalog.dish.dto.request.DishRequestDTO;

import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("${api.prefix}/dishes")
@AllArgsConstructor
public class DishController implements DishControllerDocs {

    private final DishService dishService;

    @Override
    @GetMapping
    public ResponseEntity<List<DishDTO>> getAllDishes(){
        return ResponseEntity.ok(dishService.getAllDishes());
    }

    @Override
    @GetMapping("/{dishId}/tags")
    public ResponseEntity<List<Tag>> getTagsByDishId(@PathVariable Integer dishId){
        return ResponseEntity.ok(dishService.getAllTags(dishId));
    }

    @Override
    @PostMapping
    public ResponseEntity<Dish> createDish(@Valid @RequestBody DishRequestDTO dishRequestDTO){
        return ResponseEntity.ok(dishService.createDish(dishRequestDTO));
    }

    @Override
    @GetMapping("/average_rating")
    public ResponseEntity<List<Float>> average_rating(){
        return ResponseEntity.ok(dishService.getAverageRatings());
    }

    @Override
    @GetMapping("/allIds")
    public ResponseEntity<List<Integer>> getAllDishId(){
        return ResponseEntity.ok(dishService.getAllDishId());
    }

    @Override
    @PostMapping("/blocking/{id}/{type}")
    public ResponseEntity<String> blocking(@PathVariable Integer id, @PathVariable Integer type){
        dishService.blocking(id, type);

        return ResponseEntity.ok("Success");
    }

}
