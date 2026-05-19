package com.example.foodie.services.impl;

import com.example.foodie.dtos.UserDishDTO;
import com.example.foodie.models.*;
import com.example.foodie.repos.DishRepository;
import com.example.foodie.repos.UserDishRepository;
import com.example.foodie.repos.UserRepository;
import com.example.foodie.services.interfaces.UserDishService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserDishImpl implements UserDishService {
    private UserDishRepository userDishRepository;
    private UserRepository userRepository;
    private DishRepository dishRepository;

    @Override
    public List<UserDish> getAllUserDishes(){
        List<UserDish> userDishes = userDishRepository.findAll();

        if(userDishes.isEmpty()){
            throw new RuntimeException("Không tồn tại userDish nào");
        }else{
            return userDishes;
        }
    }

    @Override
    public List<UserDish> getAllUserDishesByUserId(Authentication authentication){
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
        List<UserDish> userDishes = userDishRepository.findAllByUser_Id(user.getId());

        // if(userDishes.isEmpty()){
        //     throw new RuntimeException("Không tồn tại userDish nào");
        // }else{
        //     return userDishes;
        // }
        return userDishes;
    }

    @Override
    public void addUserDish(Authentication authentication, UserDishDTO userDishDTO){
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
        Dish dish = dishRepository.findById(userDishDTO.getDishId())
                .orElseThrow(() -> new RuntimeException("Dish không tồn tại"));

        Optional<UserDish> existingUserDish = userDishRepository.findByUser_IdAndDish_Id(user.getId(), userDishDTO.getDishId());
        if (existingUserDish.isPresent()) {
            UserDish userDish = existingUserDish.get();
            userDish.setQuantity(userDish.getQuantity() + userDishDTO.getQuantity());
            userDishRepository.save(userDish);
        } else {
            UserDish newUserDish = UserDish.builder()
                    .user(user)
                    .dish(dish)
                    .quantity(userDishDTO.getQuantity())
                    .build();
            userDishRepository.save(newUserDish);
        }
    }

    @Override
    public void deleteUserDishById(Integer userDishId){
        userDishRepository.deleteById(userDishId);
    }

    @Override
    public void updateQuantity(Integer userDishId, Integer quantity){
        UserDish userDish = userDishRepository.findById(userDishId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy món trong giỏ"));

        if (quantity <= 0){
            userDishRepository.deleteById(userDishId);
            return;
        }

        userDish.setQuantity(quantity);

        userDishRepository.save(userDish);
    }

}
