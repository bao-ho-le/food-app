package com.example.foodie.order.repository;

import com.example.foodie.order.entity.OrderDish;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderDishRepository extends JpaRepository<OrderDish, Integer> {
    Optional<OrderDish> findByDish_Id(Integer dishId);
    List<OrderDish> findByOrder_Id(Integer orderId);
    List<OrderDish> findAllByDishId(Integer dishId);
}
