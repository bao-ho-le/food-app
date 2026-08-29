package com.example.foodie.ordering.userdish.service;

import com.example.foodie.catalog.dish.entity.Dish;
import com.example.foodie.catalog.dish.repository.DishRepository;
import com.example.foodie.common.exception.ErrorCode;
import com.example.foodie.common.exception.business_exception.CatalogException;
import com.example.foodie.common.exception.business_exception.OrderingException;
import com.example.foodie.identity.user.entity.User;
import com.example.foodie.identity.user.helper.UserHelper;
import com.example.foodie.ordering.userdish.dto.request.UserDishDTO;
import com.example.foodie.ordering.userdish.dto.response.UserDishResponseDTO;
import com.example.foodie.ordering.userdish.entity.UserDish;
import com.example.foodie.ordering.userdish.helper.UserDishHelper;
import com.example.foodie.ordering.userdish.mapper.UserDishMapper;
import com.example.foodie.ordering.userdish.repository.UserDishRepository;
import jakarta.transaction.Transactional;
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
    private final UserDishMapper userDishMapper;

    @Override
    public List<UserDish> getAllUserDishes(){
        List<UserDish> userDishes = userDishRepository.findAll();

        if(userDishes.isEmpty()){
            throw new OrderingException(ErrorCode.USERDISH_NOT_FOUND);
        }
        return userDishes;
    }

    @Override
    public List<UserDishResponseDTO> getAllUserDishesByUserId(Authentication authentication){
        User user = userHelper.getUserFromAuthentication(authentication);

        return userDishRepository.findAllByUser_Id(user.getId()).stream()
                .map(userDishMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void addUserDish(Authentication authentication, UserDishDTO userDishDTO){
        userDishHelper.validateUserDishRequest(userDishDTO);

        User user = userHelper.getUserFromAuthentication(authentication);
        Dish dish = dishRepository.findById(userDishDTO.getDishId())
                .orElseThrow(() -> new CatalogException(ErrorCode.DISH_NOT_FOUND));

        Optional<UserDish> existingUserDish =
                userDishRepository.findByUser_IdAndDish_Id(user.getId(), userDishDTO.getDishId());

        int requestedTotalQuantity = userDishDTO.getQuantity()
                + existingUserDish.map(UserDish::getQuantity).orElse(0);

        if (dish.getStockQuantity() < requestedTotalQuantity) {
            throw new CatalogException(ErrorCode.DISH_OUT_OF_STOCK);
        }

        userDishRepository.upsertQuantity(user.getId(), dish.getId(), userDishDTO.getQuantity());
    }

    @Transactional
    @Override
    public void deleteUserDishById(Authentication authentication, Integer userDishId){
        userDishHelper.validateUserDishId(userDishId);

        User user = userHelper.getUserFromAuthentication(authentication);

        if (!userDishRepository.existsByIdAndUser_Id(userDishId, user.getId())) {
            throw new OrderingException(ErrorCode.USERDISH_NOT_FOUND);
        }
        userDishRepository.deleteByIdAndUser_Id(userDishId, user.getId());
    }

    @Transactional
    @Override
    public void updateQuantity(Authentication authentication, Integer userDishId, Integer quantity){
        userDishHelper.validateUserDishId(userDishId);
        userDishHelper.validateQuantity(quantity);

        User user = userHelper.getUserFromAuthentication(authentication);

        UserDish userDish = userDishRepository.findByIdAndUser_Id(userDishId, user.getId())
                .orElseThrow(() -> new OrderingException(ErrorCode.USERDISH_NOT_FOUND));

        if (quantity <= 0){
            userDishRepository.deleteByIdAndUser_Id(userDishId, user.getId());
            return;
        }

        if (userDish.getDish().getStockQuantity() < quantity) {
            throw new CatalogException(ErrorCode.DISH_OUT_OF_STOCK);
        }

        userDish.setQuantity(quantity);

        userDishRepository.save(userDish);
    }
}
