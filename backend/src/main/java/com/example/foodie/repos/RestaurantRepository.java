package com.example.foodie.repos;

import com.example.foodie.models.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantRepository extends JpaRepository<Restaurant, Integer> {  
    Restaurant findById(int id);
    boolean existsByName(String name);
    boolean existsByPhoneNumber(String phoneNumber);
}
