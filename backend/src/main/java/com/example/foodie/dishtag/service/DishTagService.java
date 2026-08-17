package com.example.foodie.dishtag.service;

import com.example.foodie.dishtag.entity.DishTag;

public interface DishTagService {
    public DishTag addTagForDish(int dish_id, int tagId);
}
