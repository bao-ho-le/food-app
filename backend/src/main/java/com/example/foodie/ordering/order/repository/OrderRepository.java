package com.example.foodie.ordering.order.repository;

import com.example.foodie.ordering.order.entity.Order;
import com.example.foodie.ordering.order.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    public List<Order> findAllByUser_Id(Integer userId);

    long countByCreatedAtBetween(Instant start, Instant end);

    List<Order> findByCreatedAtBetween(Instant start, Instant end);

    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE o.status = :status AND o.createdAt BETWEEN :start AND :end")
    Float sumTotalPriceByStatusAndCreatedAtBetween(@Param("status") Status status, @Param("start") Instant start, @Param("end") Instant end);
}
