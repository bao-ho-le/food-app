package com.example.foodie.catalog.dishtag.service;

import com.example.foodie.catalog.dishtag.entity.DishTag;

public interface DishTagService {
    public DishTag addTagForDish(int dish_id, int tagId);

    public void removeAllTagsForDish(int dishId);
}
