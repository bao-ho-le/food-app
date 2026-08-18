package com.example.foodie.catalog.restaurant.controller;

import com.example.foodie.catalog.restaurant.entity.Restaurant;
import com.example.foodie.catalog.restaurant.service.RestaurantService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("${api.prefix}/restaurants")
@AllArgsConstructor
public class RestaurantController implements RestaurantControllerDocs {
    private final RestaurantService restaurantService;

    @Override
    @GetMapping
    public ResponseEntity<List<Restaurant>> getAllRestaurants(){
        return ResponseEntity.ok(restaurantService.getAllRestaurants());
    }

}
