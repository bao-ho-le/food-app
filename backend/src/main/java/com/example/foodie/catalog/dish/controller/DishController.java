package com.example.foodie.catalog.dish.controller;

import com.example.foodie.catalog.dish.dto.response.DishDTO;
import com.example.foodie.catalog.tag.dto.response.TagResponseDTO;
import com.example.foodie.catalog.dish.service.DishService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


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
    public ResponseEntity<List<TagResponseDTO>> getTagsByDishId(@PathVariable Integer dishId){
        return ResponseEntity.ok(dishService.getAllTags(dishId));
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

}
