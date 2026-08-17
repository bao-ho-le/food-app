package com.example.foodie.ordering.userdish.service;


import com.example.foodie.ordering.userdish.dto.request.UserDishDTO;
import com.example.foodie.ordering.userdish.entity.UserDish;

import java.util.List;

import org.springframework.security.core.Authentication;

public interface UserDishService {
    public List<UserDish> getAllUserDishes();
    public List<UserDish> getAllUserDishesByUserId(Authentication authentication);
    public void addUserDish(Authentication authentication, UserDishDTO userDishDTO);
    public void deleteUserDishById(Integer userDishId);
    public void updateQuantity(Integer userDishId, Integer quantity);
}
