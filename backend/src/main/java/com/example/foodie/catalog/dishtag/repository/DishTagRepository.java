package com.example.foodie.catalog.dishtag.repository;

import com.example.foodie.catalog.dish.entity.Dish;
import com.example.foodie.catalog.dishtag.entity.DishTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DishTagRepository extends JpaRepository<DishTag, Integer> {
    boolean existsByDish_IdAndTag_Id(int dishId, int tagId);

    void deleteByDish_Id(int dishId);

    @Query("SELECT dt.tag.name FROM DishTag dt WHERE dt.dish.id = :dishId")
    List<String> findTagNamesByDishId(@Param("dishId") int dishId);
}
