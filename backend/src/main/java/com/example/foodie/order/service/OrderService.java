package com.example.foodie.order.service;

import com.example.foodie.common.base.BaseService;
import com.example.foodie.order.dto.response.OrderDishResponseDTO;
import com.example.foodie.order.entity.Order;

import java.util.List;

import org.springframework.security.core.Authentication;

public interface OrderService extends BaseService<Order>{
    public Order createOrder(Authentication authentication, Integer addressId);
    public List<Order> getAllOrdersByUserId(Authentication authentication);
    public List<OrderDishResponseDTO> getAllOrderItems(Integer orderId);
}
