package com.example.foodie.controllers;

import com.example.foodie.dtos.DishDTO;
import com.example.foodie.models.Dish;
import com.example.foodie.models.Tag;
import com.example.foodie.services.interfaces.DishService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.example.foodie.dtos.DishRequestDTO;

import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("${api.prefix}/dishes")
@AllArgsConstructor
public class DishController {

    private DishService dishService;

    @GetMapping
    public ResponseEntity<?> getAllDishes(){

        try {
            List<DishDTO> dishes = dishService.getAllDishes();

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(dishes);
        }catch (Exception e){
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    @GetMapping("/{dishId}/tags")
    public ResponseEntity<?> getTagsByDishId(@PathVariable Integer dishId){
        try {
            List<Tag> tags = dishService.getAllTags(dishId); // gọi service để lấy tag

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(tags);
        } catch (Exception e){
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createDish(@Valid @RequestBody DishRequestDTO dishRequestDTO){

        try{
            Dish newDish = dishService.createDish(dishRequestDTO);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(newDish);
        }catch (Exception e){
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @GetMapping("/average_rating")
    public ResponseEntity<?> average_rating(){

        try{
            List<Float> dishes_rating = dishService.getAverageRatings();

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(dishes_rating);
        }catch (Exception e){
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @GetMapping("/allIds")
    public ResponseEntity<?> getAllDishId(){

        try{
            List<Integer> dallDishId = dishService.getAllDishId();

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(dallDishId);
        }catch (Exception e){
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/blocking/{id}/{type}")
    public ResponseEntity<?> blocking(@PathVariable Integer id, @PathVariable Integer type){
        try{
           dishService.blocking(id, type);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body("Success");
        }catch (Exception e){
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
        
    }
    
}
