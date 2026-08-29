package com.example.foodie.ordering.userdish.repository;

import com.example.foodie.ordering.userdish.entity.UserDish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserDishRepository extends JpaRepository<UserDish, Integer> {
    boolean existsByUser_IdAndDish_Id(int userId, int dishId);
    List<UserDish> findAllByUser_Id(Integer userId);
    Optional<UserDish> findByUser_IdAndDish_Id(Integer userId, Integer dishId);
    boolean existsByIdAndUser_Id(Integer userDishId, Integer userId);
    Optional<UserDish> findByIdAndUser_Id(Integer userDishId, Integer userId);
    void deleteByIdAndUser_Id(Integer userDishId, Integer userId);

    @Modifying
    @Query(value = """
        INSERT INTO user_dish (user_id, dish_id, quantity, created_at, updated_at)
        VALUES (:userId, :dishId, :quantity, NOW(), NOW())
        ON DUPLICATE KEY UPDATE quantity = quantity + VALUES(quantity), updated_at = NOW()
        """, nativeQuery = true)
    void upsertQuantity(@Param("userId") Integer userId, @Param("dishId") Integer dishId, @Param("quantity") Integer quantity);
}
