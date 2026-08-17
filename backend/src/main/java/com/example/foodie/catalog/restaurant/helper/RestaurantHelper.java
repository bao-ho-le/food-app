package com.example.foodie.catalog.restaurant.helper;

import com.example.foodie.catalog.restaurant.entity.Restaurant;
import org.springframework.stereotype.Component;

@Component
public class RestaurantHelper {

    public void validateRestaurantId(Integer restaurantId) {
        if (restaurantId == null || restaurantId <= 0) {
            throw new RuntimeException("Id nhà hàng không hợp lệ");
        }
    }

    public void validateRestaurantRequest(Restaurant restaurant) {
        if (restaurant == null) {
            throw new RuntimeException("Thông tin nhà hàng không được để trống");
        }
        if (restaurant.getName() == null || restaurant.getName().isBlank()) {
            throw new RuntimeException("Tên nhà hàng không được để trống");
        }
        if (restaurant.getName().length() > 255) {
            throw new RuntimeException("Tên nhà hàng không được dài quá 255 ký tự");
        }
    }

    public void validateBlockingType(Integer type) {
        if (type == null || (type != 0 && type != 1)) {
            throw new RuntimeException("Loại không hợp lệ");
        }
    }
}
