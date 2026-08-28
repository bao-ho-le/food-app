package com.example.foodie.ordering.order.service;

import com.example.foodie.ordering.order.dto.response.OrderDishResponseDTO;
import com.example.foodie.ordering.order.entity.Order;
import com.example.foodie.ordering.order.enums.Status;

import java.util.List;

import org.springframework.security.core.Authentication;

public interface OrderService {
    public List<Order> getAll();
    public Order getById(Integer id);
    public void deleteById(Integer id);
    public Order createOrder(Authentication authentication, Integer addressId);
    public List<Order> getAllOrdersByUserId(Authentication authentication);
    public List<OrderDishResponseDTO> getAllOrderItems(Integer orderId);
    public List<OrderDishResponseDTO> getOwnOrderItems(Authentication authentication, Integer orderId);
    public Order updateStatus(Integer orderId, Status newStatus);
    public Order cancelOwnOrder(Authentication authentication, Integer orderId);
    public Order confirmOwnOrderReceived(Authentication authentication, Integer orderId);
}
