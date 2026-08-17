package com.example.foodie.dish.mapper;

import com.example.foodie.dish.dto.request.DishRequestDTO;
import com.example.foodie.dish.dto.response.DishDTO;
import com.example.foodie.dish.entity.Dish;
import com.example.foodie.restaurant.entity.Restaurant;
import com.example.foodie.tag.entity.Tag;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DishMapper {

    public Dish toEntity(DishRequestDTO dishRequestDTO, Restaurant restaurant) {
        return Dish.builder()
                .name(dishRequestDTO.getName())
                .price(dishRequestDTO.getPrice())
                .restaurant(restaurant)
                .isAvailable(true)
                .build();
    }

    public DishDTO toDto(Dish dish, float rating, List<Tag> tags, String imageUrl) {
        return DishDTO.builder()
                .id(dish.getId())
                .name(dish.getName())
                .price(dish.getPrice())
                .isAvailable(dish.isAvailable())
                .restaurant(dish.getRestaurant())
                .rating(rating)
                .tags(tags)
                .url(imageUrl)
                .build();
    }
}
