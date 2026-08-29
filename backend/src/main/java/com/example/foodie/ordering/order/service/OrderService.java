package com.example.foodie.ordering.order.service;

import com.example.foodie.ordering.order.dto.response.OrderDishResponseDTO;
import com.example.foodie.ordering.order.dto.response.OrderResponseDTO;
import com.example.foodie.ordering.order.enums.Status;

import java.util.List;

import org.springframework.security.core.Authentication;

public interface OrderService {
    public List<OrderResponseDTO> getAll();
    public OrderResponseDTO getById(Integer id);
    public void deleteById(Integer id);
    public OrderResponseDTO createOrder(Authentication authentication, Integer addressId);
    public List<OrderResponseDTO> getAllOrdersByUserId(Authentication authentication);
    public List<OrderDishResponseDTO> getAllOrderItems(Integer orderId);
    public List<OrderDishResponseDTO> getOwnOrderItems(Authentication authentication, Integer orderId);
    public OrderResponseDTO updateStatus(Integer orderId, Status newStatus);
    public OrderResponseDTO cancelOwnOrder(Authentication authentication, Integer orderId);
    public OrderResponseDTO confirmOwnOrderReceived(Authentication authentication, Integer orderId);
}
