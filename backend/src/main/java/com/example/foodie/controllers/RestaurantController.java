package com.example.foodie.controllers;

import com.example.foodie.models.Restaurant;
import com.example.foodie.services.interfaces.RestaurantService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;



@RestController
@RequestMapping("${api.prefix}/restaurants")
@AllArgsConstructor
public class RestaurantController {
    private RestaurantService restaurantService;

    @GetMapping
    public ResponseEntity<?> getAllRestaurants(){
        try{
            List<Restaurant> restaurants = restaurantService.getAllRestaurants();

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(restaurants);
        }catch(Exception e){
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createRestaurant(@Valid @RequestBody Restaurant restaurant){
        try {
            Restaurant newRestaurant = restaurantService.createRestaurant(restaurant);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(newRestaurant);
        }catch(Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRestaurant(@PathVariable Integer id, @RequestBody Restaurant restaurant) {
        try{
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(restaurantService.updateRestaurant(id, restaurant));
        }catch(Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }



    @PostMapping("/blocking/{id}/{type}")
    public ResponseEntity<?> blocking(@PathVariable Integer id, @PathVariable Integer type){
        try{
           restaurantService.blocking(id, type);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body("Success");
        }catch(Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }
    
}
