package com.example.foodie.ordering.userdish.helper;

import com.example.foodie.common.exception.ErrorCode;
import com.example.foodie.common.exception.business_exception.OrderingException;
import com.example.foodie.ordering.userdish.dto.request.UserDishDTO;
import org.springframework.stereotype.Component;

@Component
public class UserDishHelper {

    public void validateUserDishId(Integer userDishId) {
        if (userDishId == null) {
            throw new OrderingException(ErrorCode.USERDISH_ID_REQUIRED);
        }
        if (userDishId <= 0) {
            throw new OrderingException(ErrorCode.USERDISH_ID_INVALID);
        }
    }

    public void validateUserDishRequest(UserDishDTO userDishDTO) {
        if (userDishDTO == null) {
            throw new OrderingException(ErrorCode.USERDISH_REQUEST_REQUIRED);
        }
        if (userDishDTO.getDishId() == null) {
            throw new OrderingException(ErrorCode.DISH_ID_REQUIRED);
        }
        if (userDishDTO.getDishId() <= 0) {
            throw new OrderingException(ErrorCode.DISH_ID_INVALID);
        }
        if (userDishDTO.getQuantity() == null || userDishDTO.getQuantity() < 1) {
            throw new OrderingException(ErrorCode.USERDISH_QUANTITY_INVALID);
        }
    }

    /* Không chặn quantity <= 0: đó là tín hiệu xoá món khỏi giỏ (xem updateQuantity) */
    public void validateQuantity(Integer quantity) {
        if (quantity == null) {
            throw new OrderingException(ErrorCode.USERDISH_QUANTITY_REQUIRED);
        }
    }
}
