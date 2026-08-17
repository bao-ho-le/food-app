package com.example.foodie.catalog.restaurant.repository;

import com.example.foodie.catalog.restaurant.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantRepository extends JpaRepository<Restaurant, Integer> {  
    Restaurant findById(int id);
    boolean existsByName(String name);
    boolean existsByPhoneNumber(String phoneNumber);
}
