package com.example.foodie.catalog.dish.helper;

import com.example.foodie.catalog.dish.dto.request.DishRequestDTO;
import org.springframework.stereotype.Component;

@Component
public class DishHelper {

    private static final int MAX_NAME_LENGTH = 255;

    public void validateDishId(Integer dishId) {
        if (dishId == null || dishId <= 0) {
            throw new RuntimeException("Id món ăn không hợp lệ");
        }
    }

    public void validateDishRequest(DishRequestDTO dishRequestDTO) {
        if (dishRequestDTO == null) {
            throw new RuntimeException("Thông tin món ăn không được để trống");
        }
        if (dishRequestDTO.getName() == null || dishRequestDTO.getName().isBlank()) {
            throw new RuntimeException("Tên món ăn không được để trống");
        }
        if (dishRequestDTO.getName().length() > MAX_NAME_LENGTH) {
            throw new RuntimeException("Tên món ăn không được dài quá " + MAX_NAME_LENGTH + " ký tự");
        }
        if (dishRequestDTO.getPrice() < 0) {
            throw new RuntimeException("Giá món ăn không được âm");
        }
        if (dishRequestDTO.getRestaurantId() == null || dishRequestDTO.getRestaurantId() <= 0) {
            throw new RuntimeException("Id nhà hàng không hợp lệ");
        }
        if (dishRequestDTO.getTags() == null) {
            throw new RuntimeException("Danh sách tag không được để trống");
        }
    }

    public void validateBlockingType(Integer type) {
        if (type == null || (type != 0 && type != 1)) {
            throw new RuntimeException("Type không hợp lệ");
        }
    }
}
