package com.example.foodie.order.mapper;

import com.example.foodie.dish.entity.Dish;
import com.example.foodie.order.dto.response.OrderDishResponseDTO;
import com.example.foodie.order.entity.Order;
import com.example.foodie.order.entity.OrderDish;
import com.example.foodie.order.enums.Status;
import com.example.foodie.user.entity.User;
import com.example.foodie.userdish.entity.UserDish;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public Order toEntity(User user, String deliveryAddress) {
        return Order.builder()
                .user(user)
                .deliveryAddress(deliveryAddress)
                .totalPrice(0f)
                .status(Status.DELIVERED)
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
