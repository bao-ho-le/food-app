package com.example.foodie.catalog.tag.repository;

import com.example.foodie.catalog.tag.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Integer> {
    @Query("SELECT t FROM DishTag dt JOIN dt.tag t WHERE dt.dish.id = :dishId")
    List<Tag> findTagsByDishId(@Param("dishId") Integer dishId);

    Optional<Tag> findByName(String name);
}
