package com.example.foodie.repos;

import com.example.foodie.models.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
    @Query("SELECT od.review FROM OrderDish od WHERE od.dish.id = :dishId AND od.review IS NOT NULL")
    List<Review> findAllByDishId(@Param("dishId") Integer dishId);

    @Query("SELECT COALESCE(AVG(od.review.rating), 0) FROM OrderDish od WHERE od.dish.id = :dishId AND od.review IS NOT NULL")
    Float findAverageRatingByDishId(@Param("dishId") Integer dishId);
}
