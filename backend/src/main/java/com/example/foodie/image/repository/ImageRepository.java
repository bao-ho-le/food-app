package com.example.foodie.image.repository;

import com.example.foodie.image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Integer> {
    List<Image> findByDish_Id(Integer dishId);
    Optional<Image> findFirstByDish_IdOrderByIdAsc(Integer dishId);
}
