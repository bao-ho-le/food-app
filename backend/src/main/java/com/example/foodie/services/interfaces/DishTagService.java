package com.example.foodie.services.interfaces;

import com.example.foodie.models.DishTag;

public interface DishTagService {
    public DishTag addTagForDish(int dish_id, int tagId);
}
