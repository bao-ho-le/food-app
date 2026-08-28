package com.example.foodie.catalog.dish.repository;

import com.example.foodie.catalog.dish.entity.Dish;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DishRepository extends JpaRepository<Dish, Integer> {
    @Query("SELECT d.id FROM Dish d")
    List<Integer> findAllDishIds();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Dish d WHERE d.id = :id")
    Optional<Dish> findByIdForUpdate(@Param("id") Integer id);
}
