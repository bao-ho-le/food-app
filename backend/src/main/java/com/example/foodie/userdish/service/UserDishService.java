package com.example.foodie.userdish.service;


import com.example.foodie.userdish.dto.request.UserDishDTO;
import com.example.foodie.userdish.entity.UserDish;

import java.util.List;

import org.springframework.security.core.Authentication;

public interface UserDishService {
    public List<UserDish> getAllUserDishes();
    public List<UserDish> getAllUserDishesByUserId(Authentication authentication);
    public void addUserDish(Authentication authentication, UserDishDTO userDishDTO);
    public void deleteUserDishById(Integer userDishId);
    public void updateQuantity(Integer userDishId, Integer quantity);
}
