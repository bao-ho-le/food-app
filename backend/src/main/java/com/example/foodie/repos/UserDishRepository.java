package com.example.foodie.repos;

import com.example.foodie.models.UserDish;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserDishRepository extends JpaRepository<UserDish, Integer> {
    boolean existsByUser_IdAndDish_Id(int userId, int dishId);
    List<UserDish> findAllByUser_Id(Integer userId);
    Optional<UserDish> findByUser_IdAndDish_Id(Integer userId, Integer dishId);
}
