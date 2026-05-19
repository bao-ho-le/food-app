package com.example.foodie.services.interfaces;

import com.example.foodie.dtos.DishDTO;
import com.example.foodie.models.Dish;
import com.example.foodie.models.Tag;
import com.example.foodie.dtos.DishRequestDTO;

import java.util.List;

public interface DishService {
    public List<DishDTO> getAllDishes();

    public Dish createDish(DishRequestDTO dishRequestDTO);

    public List<Tag> getAllTags(Integer dishId);

    public List<Float> getAverageRatings();

    public List<Integer> getAllDishId();

    public void blocking(Integer dishId, Integer type);
}
