package com.example.foodie.userdish.helper;

import com.example.foodie.userdish.dto.request.UserDishDTO;
import org.springframework.stereotype.Component;

@Component
public class UserDishHelper {

    public void validateUserDishId(Integer userDishId) {
        if (userDishId == null || userDishId <= 0) {
            throw new RuntimeException("Id món trong giỏ không hợp lệ");
        }
    }

    public void validateUserDishRequest(UserDishDTO userDishDTO) {
        if (userDishDTO == null) {
            throw new RuntimeException("Thông tin món trong giỏ không được để trống");
        }
        if (userDishDTO.getDishId() == null || userDishDTO.getDishId() <= 0) {
            throw new RuntimeException("Id món ăn không hợp lệ");
        }
        if (userDishDTO.getQuantity() == null || userDishDTO.getQuantity() < 1) {
            throw new RuntimeException("Số lượng phải >= 1");
        }
    }

    /* Không chặn quantity <= 0: đó là tín hiệu xoá món khỏi giỏ (xem updateQuantity) */
    public void validateQuantity(Integer quantity) {
        if (quantity == null) {
            throw new RuntimeException("Số lượng không được để trống");
        }
    }
}
