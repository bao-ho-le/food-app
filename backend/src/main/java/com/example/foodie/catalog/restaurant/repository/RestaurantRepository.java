package com.example.foodie.catalog.restaurant.repository;

import com.example.foodie.catalog.restaurant.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;

public interface RestaurantRepository extends JpaRepository<Restaurant, Integer> {
    Restaurant findById(int id);
    boolean existsByName(String name);
    boolean existsByPhoneNumber(String phoneNumber);

    long countByCreatedAtBetween(Instant start, Instant end);

    @Query("SELECT COUNT(r) FROM Restaurant r WHERE r.isAvailable = true")
    long countActiveRestaurants();
}
