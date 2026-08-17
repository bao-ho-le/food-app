package com.example.foodie.ordering.order.repository;

import com.example.foodie.ordering.order.entity.Order;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    public List<Order> findAllByUser_Id(Integer userId);
}
