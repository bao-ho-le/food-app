package com.example.foodie.catalog.dishtag.controller;

import com.example.foodie.catalog.dishtag.entity.DishTag;
import com.example.foodie.catalog.dishtag.service.DishTagService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/dish-tag")
@AllArgsConstructor
public class DishTagController {
    private final DishTagService dishTagService;

    @PostMapping("/{dish_id}")
    public ResponseEntity<DishTag> addTagForDish(@PathVariable int dish_id, @RequestParam int tagId){
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(dishTagService.addTagForDish(dish_id, tagId));
    }
}
