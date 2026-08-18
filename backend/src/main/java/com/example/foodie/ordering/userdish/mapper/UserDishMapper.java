package com.example.foodie.ordering.userdish.mapper;

import com.example.foodie.catalog.dish.entity.Dish;
import com.example.foodie.catalog.image.repository.ImageRepository;
import com.example.foodie.catalog.restaurant.entity.Restaurant;
import com.example.foodie.ordering.userdish.dto.response.DishSummaryDTO;
import com.example.foodie.ordering.userdish.dto.response.RestaurantSummaryDTO;
import com.example.foodie.ordering.userdish.dto.response.UserDishResponseDTO;
import com.example.foodie.ordering.userdish.entity.UserDish;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserDishMapper {

    private final ImageRepository imageRepository;

    public UserDishResponseDTO toDto(UserDish userDish) {
        return UserDishResponseDTO.builder()
                .id(userDish.getId())
                .quantity(userDish.getQuantity())
                .dish(toDishSummary(userDish.getDish()))
                .build();
    }

    private DishSummaryDTO toDishSummary(Dish dish) {
        Restaurant restaurant = (Restaurant) Hibernate.unproxy(dish.getRestaurant());

        return DishSummaryDTO.builder()
                .id(dish.getId())
                .name(dish.getName())
                .price(dish.getPrice())
                .available(dish.isAvailable())
                .url(resolveImageUrl(dish))
                .restaurant(restaurant != null
                        ? RestaurantSummaryDTO.builder().id(restaurant.getId()).name(restaurant.getName()).build()
                        : null)
                .build();
    }

    private String resolveImageUrl(Dish dish) {
        return imageRepository.findFirstByDish_IdOrderByIdAsc(dish.getId())
                .map(image -> image.getUrl())
                .orElse("");
    }
}
