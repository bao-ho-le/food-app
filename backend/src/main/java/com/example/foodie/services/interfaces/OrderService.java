package com.example.foodie.services.interfaces;

import com.example.foodie.dtos.OrderDishResponseDTO;
import com.example.foodie.models.Order;

import java.util.List;

import org.springframework.security.core.Authentication;

public interface OrderService extends BaseService<Order>{
    public Order createOrder(Authentication authentication, Integer addressId);
    public List<Order> getAllOrdersByUserId(Authentication authentication);
    public List<OrderDishResponseDTO> getAllOrderItems(Integer orderId);
}
