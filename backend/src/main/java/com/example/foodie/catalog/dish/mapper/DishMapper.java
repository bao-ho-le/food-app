package com.example.foodie.catalog.dish.mapper;

import com.example.foodie.catalog.dish.dto.request.DishRequestDTO;
import com.example.foodie.catalog.dish.dto.response.DishDTO;
import com.example.foodie.catalog.dish.entity.Dish;
import com.example.foodie.catalog.restaurant.entity.Restaurant;
import com.example.foodie.catalog.tag.entity.Tag;
import org.hibernate.Hibernate;
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
                .restaurant((Restaurant) Hibernate.unproxy(dish.getRestaurant()))
                .rating(rating)
                .tags(tags)
                .url(imageUrl)
                .build();
    }
}
