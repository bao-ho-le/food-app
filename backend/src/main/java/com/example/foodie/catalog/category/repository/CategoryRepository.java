package com.example.foodie.catalog.category.repository;

import com.example.foodie.catalog.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
}
