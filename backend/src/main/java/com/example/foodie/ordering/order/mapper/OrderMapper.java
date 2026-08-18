package com.example.foodie.ordering.order.mapper;

import com.example.foodie.catalog.dish.entity.Dish;
import com.example.foodie.ordering.order.dto.response.OrderDishResponseDTO;
import com.example.foodie.ordering.order.entity.Order;
import com.example.foodie.ordering.order.entity.OrderDish;
import com.example.foodie.identity.user.entity.User;
import com.example.foodie.ordering.userdish.entity.UserDish;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public Order toEntity(User user, String deliveryAddress) {
        return Order.builder()
                .user(user)
                .deliveryAddress(deliveryAddress)
                .totalPrice(0f)
                .build();
    }

    public OrderDish toOrderDish(UserDish userDish, Order order) {
        return OrderDish.builder()
                .dish(userDish.getDish())
                .quantity(userDish.getQuantity())
                .price(userDish.getDish().getPrice())
                .order(order)
                .build();
    }

    public OrderDishResponseDTO toOrderDishResponse(OrderDish orderDish, String imageUrl) {
        Dish dish = orderDish.getDish();

        return OrderDishResponseDTO.builder()
                .id(orderDish.getId())
                .dishId(dish.getId())
                .dishName(dish.getName())
                .quantity(orderDish.getQuantity())
                .price(orderDish.getPrice())
                .imageUrl(imageUrl)
                .restaurantName(dish.getRestaurant().getName())
                .reviewed(orderDish.getReview() != null)
                .build();
    }
}
