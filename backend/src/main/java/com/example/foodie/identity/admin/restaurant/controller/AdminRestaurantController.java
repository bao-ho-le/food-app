package com.example.foodie.identity.admin.restaurant.controller;

import com.example.foodie.catalog.restaurant.entity.Restaurant;
import com.example.foodie.catalog.restaurant.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/admin/restaurants")
@AllArgsConstructor
public class AdminRestaurantController implements AdminRestaurantControllerDocs {
    private final RestaurantService restaurantService;

    @Override
    @PostMapping
    public ResponseEntity<Restaurant> createRestaurant(@Valid @RequestBody Restaurant restaurant){
        return ResponseEntity.ok(restaurantService.createRestaurant(restaurant));
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<Restaurant> updateRestaurant(@PathVariable Integer id, @RequestBody Restaurant restaurant) {
        return ResponseEntity.ok(restaurantService.updateRestaurant(id, restaurant));
    }

    @Override
    @PostMapping("/blocking/{id}/{type}")
    public ResponseEntity<String> blocking(@PathVariable Integer id, @PathVariable Integer type){
        restaurantService.blocking(id, type);

        return ResponseEntity.ok("Success");
    }
}
