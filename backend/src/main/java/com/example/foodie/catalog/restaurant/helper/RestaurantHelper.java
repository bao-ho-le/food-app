package com.example.foodie.catalog.restaurant.helper;

import com.example.foodie.catalog.restaurant.entity.Restaurant;
import com.example.foodie.common.exception.ErrorCode;
import com.example.foodie.common.exception.business_exception.CatalogException;
import org.springframework.stereotype.Component;

@Component
public class RestaurantHelper {

    public void validateRestaurantId(Integer restaurantId) {
        if (restaurantId == null) {
            throw new CatalogException(ErrorCode.RESTAURANT_ID_REQUIRED);
        }
        if (restaurantId <= 0) {
            throw new CatalogException(ErrorCode.RESTAURANT_ID_INVALID);
        }
    }

    public void validateRestaurantRequest(Restaurant restaurant) {
        if (restaurant == null) {
            throw new CatalogException(ErrorCode.RESTAURANT_REQUEST_REQUIRED);
        }
        if (restaurant.getName() == null || restaurant.getName().isBlank()) {
            throw new CatalogException(ErrorCode.RESTAURANT_NAME_REQUIRED);
        }
        if (restaurant.getName().length() > 255) {
            throw new CatalogException(ErrorCode.RESTAURANT_NAME_TOO_LONG);
        }
    }

    public void validateBlockingType(Integer type) {
        if (type == null || (type != 0 && type != 1)) {
            throw new CatalogException(ErrorCode.RESTAURANT_BLOCK_TYPE_INVALID);
        }
    }
}
