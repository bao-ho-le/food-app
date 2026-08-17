package com.example.foodie.catalog.dish.repository;

import com.example.foodie.catalog.dish.entity.Dish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DishRepository extends JpaRepository<Dish, Integer> {
    @Query("SELECT d.id FROM Dish d")
    List<Integer> findAllDishIds();
}
