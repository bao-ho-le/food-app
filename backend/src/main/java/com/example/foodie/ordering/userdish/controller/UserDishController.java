package com.example.foodie.ordering.userdish.controller;

import com.example.foodie.ordering.userdish.dto.request.UpdateDishQuantityDTO;
import com.example.foodie.ordering.userdish.dto.request.UserDishDTO;
import com.example.foodie.ordering.userdish.dto.response.UserDishResponseDTO;
import com.example.foodie.ordering.userdish.service.UserDishService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/user-dishes")
@AllArgsConstructor
public class UserDishController implements UserDishControllerDocs {
    private final UserDishService userDishService;

    @Override
    @GetMapping
    public ResponseEntity<List<UserDishResponseDTO>> getAllUserDishes(Authentication authentication){
        return ResponseEntity.ok(userDishService.getAllUserDishesByUserId(authentication));
    }

    @Override
    @PostMapping
    public ResponseEntity<Void> addUserDish(Authentication authentication, @Valid @RequestBody UserDishDTO userDishDTO){
        userDishService.addUserDish(authentication, userDishDTO);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    @DeleteMapping("/{user_dish_id}")
    public ResponseEntity<Void> deleteById(
            Authentication authentication,
            @PathVariable(name="user_dish_id") Integer userDishId){
        userDishService.deleteUserDishById(authentication, userDishId);

        return ResponseEntity.ok().build();
    }

    @Override
    @PutMapping
    public ResponseEntity<Void> updateQuantity(Authentication authentication, @Valid @RequestBody UpdateDishQuantityDTO dto){
        userDishService.updateQuantity(authentication, dto.getUserDishId(), dto.getQuantity());

        return ResponseEntity.ok().build();
    }
}
