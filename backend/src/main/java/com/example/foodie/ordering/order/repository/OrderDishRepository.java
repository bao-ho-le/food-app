package com.example.foodie.ordering.order.repository;

import com.example.foodie.ordering.order.entity.OrderDish;
import com.example.foodie.ordering.order.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderDishRepository extends JpaRepository<OrderDish, Integer> {
    Optional<OrderDish> findByDish_Id(Integer dishId);
    List<OrderDish> findByOrder_Id(Integer orderId);
    List<OrderDish> findAllByDishId(Integer dishId);

    @Query("SELECT od.dish.id, SUM(od.quantity) FROM OrderDish od WHERE od.order.status = :status GROUP BY od.dish.id ORDER BY SUM(od.quantity) DESC")
    List<Object[]> sumQuantityByDishForOrderStatus(@Param("status") Status status);
}
