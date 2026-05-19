package com.example.foodie.controllers;

import com.example.foodie.models.DishTag;
import com.example.foodie.services.interfaces.DishTagService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/dish-tag")
@AllArgsConstructor
public class DishTagController {
    private DishTagService dishTagService;

    @PostMapping("/{dish_id}")
    public ResponseEntity<?> addTagForDish(@PathVariable int dish_id, @RequestParam int tagId){
        try{
            DishTag newDishTag = dishTagService.addTagForDish(dish_id, tagId);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(newDishTag);
        }catch (Exception e){
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }
}
