package com.example.foodie.repos;

import com.example.foodie.models.Dish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DishRepository extends JpaRepository<Dish, Integer> {
    @Query("SELECT d.id FROM Dish d")
    List<Integer> findAllDishIds();
}
