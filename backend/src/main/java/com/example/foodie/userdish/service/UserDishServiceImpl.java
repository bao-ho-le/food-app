package com.example.foodie.userdish.service;

import com.example.foodie.dish.entity.Dish;
import com.example.foodie.dish.repository.DishRepository;
import com.example.foodie.user.entity.User;
import com.example.foodie.user.helper.UserHelper;
import com.example.foodie.userdish.dto.request.UserDishDTO;
import com.example.foodie.userdish.entity.UserDish;
import com.example.foodie.userdish.helper.UserDishHelper;
import com.example.foodie.userdish.repository.UserDishRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserDishServiceImpl implements UserDishService {
    private final UserDishRepository userDishRepository;
    private final DishRepository dishRepository;
    private final UserDishHelper userDishHelper;
    private final UserHelper userHelper;

    @Override
    public List<UserDish> getAllUserDishes(){
        List<UserDish> userDishes = userDishRepository.findAll();

        if(userDishes.isEmpty()){
            throw new RuntimeException("Không tồn tại userDish nào");
        }
        return userDishes;
    }

    @Override
    public List<UserDish> getAllUserDishesByUserId(Authentication authentication){
        User user = userHelper.getUserFromAuthentication(authentication);

        return userDishRepository.findAllByUser_Id(user.getId());
    }

    @Override
    public void addUserDish(Authentication authentication, UserDishDTO userDishDTO){
        userDishHelper.validateUserDishRequest(userDishDTO);

        User user = userHelper.getUserFromAuthentication(authentication);
        Dish dish = dishRepository.findById(userDishDTO.getDishId())
                .orElseThrow(() -> new RuntimeException("Dish không tồn tại"));

        Optional<UserDish> existingUserDish =
                userDishRepository.findByUser_IdAndDish_Id(user.getId(), userDishDTO.getDishId());

        if (existingUserDish.isPresent()) {
            UserDish userDish = existingUserDish.get();
            userDish.setQuantity(userDish.getQuantity() + userDishDTO.getQuantity());
            userDishRepository.save(userDish);
        } else {
            userDishRepository.save(UserDish.builder()
                    .user(user)
                    .dish(dish)
                    .quantity(userDishDTO.getQuantity())
                    .build());
        }
    }

    @Override
    public void deleteUserDishById(Integer userDishId){
        userDishHelper.validateUserDishId(userDishId);

        userDishRepository.deleteById(userDishId);
    }

    @Override
    public void updateQuantity(Integer userDishId, Integer quantity){
        userDishHelper.validateUserDishId(userDishId);
        userDishHelper.validateQuantity(quantity);

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
