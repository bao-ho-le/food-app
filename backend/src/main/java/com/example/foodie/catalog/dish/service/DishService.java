package com.example.foodie.catalog.dish.service;

import com.example.foodie.catalog.dish.dto.response.DishDTO;
import com.example.foodie.catalog.dish.entity.Dish;
import com.example.foodie.catalog.tag.entity.Tag;
import com.example.foodie.catalog.dish.dto.request.DishRequestDTO;

import java.util.List;

public interface DishService {
    public List<DishDTO> getAllDishes();

    public Dish createDish(DishRequestDTO dishRequestDTO);

    public List<Tag> getAllTags(Integer dishId);

    public List<Float> getAverageRatings();

    public List<Integer> getAllDishId();

    public void blocking(Integer dishId, Integer type);
}
