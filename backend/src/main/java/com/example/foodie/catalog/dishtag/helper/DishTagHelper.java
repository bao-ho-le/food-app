package com.example.foodie.catalog.dishtag.helper;

import com.example.foodie.common.exception.ErrorCode;
import com.example.foodie.common.exception.business_exception.CatalogException;
import org.springframework.stereotype.Component;

@Component
public class DishTagHelper {

    public void validateDishId(int dishId) {
        if (dishId <= 0) {
            throw new CatalogException(ErrorCode.DISH_ID_INVALID);
        }
    }

    public void validateTagId(int tagId) {
        if (tagId <= 0) {
            throw new CatalogException(ErrorCode.TAG_ID_INVALID);
        }
    }
}
