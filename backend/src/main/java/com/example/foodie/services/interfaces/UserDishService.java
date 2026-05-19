package com.example.foodie.services.interfaces;


import com.example.foodie.dtos.UserDishDTO;
import com.example.foodie.models.UserDish;

import java.util.List;

import org.springframework.security.core.Authentication;

public interface UserDishService {
    public List<UserDish> getAllUserDishes();
    public List<UserDish> getAllUserDishesByUserId(Authentication authentication);
    public void addUserDish(Authentication authentication, UserDishDTO userDishDTO);
    public void deleteUserDishById(Integer userDishId);
    public void updateQuantity(Integer userDishId, Integer quantity);
}
