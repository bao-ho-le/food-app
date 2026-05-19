package com.example.foodie.repos;

import com.example.foodie.models.Order;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    public List<Order> findAllByUser_Id(Integer userId);
}
