package com.example.foodie.ordering.userdish.service;


import com.example.foodie.ordering.userdish.dto.request.UserDishDTO;
import com.example.foodie.ordering.userdish.dto.response.UserDishResponseDTO;
import com.example.foodie.ordering.userdish.entity.UserDish;

import java.util.List;

import org.springframework.security.core.Authentication;

public interface UserDishService {
    public List<UserDish> getAllUserDishes();
    public List<UserDishResponseDTO> getAllUserDishesByUserId(Authentication authentication);
    public void addUserDish(Authentication authentication, UserDishDTO userDishDTO);
    public void deleteUserDishById(Authentication authentication, Integer userDishId);
    public void updateQuantity(Authentication authentication, Integer userDishId, Integer quantity);
}
