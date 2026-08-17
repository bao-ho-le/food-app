package com.example.foodie.catalog.dishtag.helper;

import org.springframework.stereotype.Component;

@Component
public class DishTagHelper {

    public void validateDishId(int dishId) {
        if (dishId <= 0) {
            throw new RuntimeException("Id món ăn không hợp lệ");
        }
    }

    public void validateTagId(int tagId) {
        if (tagId <= 0) {
            throw new RuntimeException("Id tag không hợp lệ");
        }
    }
}
