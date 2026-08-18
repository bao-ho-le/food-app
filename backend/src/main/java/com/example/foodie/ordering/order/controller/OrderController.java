package com.example.foodie.ordering.order.controller;

import com.example.foodie.ordering.order.dto.request.OrderDTO;
import com.example.foodie.ordering.order.dto.response.OrderDishResponseDTO;
import com.example.foodie.ordering.order.entity.Order;
import com.example.foodie.ordering.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/orders")
@AllArgsConstructor
public class OrderController implements OrderControllerDocs {
    private final OrderService orderService;

    @Override
    @GetMapping
    public ResponseEntity<List<Order>> getAll(){
        return ResponseEntity.ok(orderService.getAll());
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<Order> getById(@PathVariable Integer id){
        return ResponseEntity.ok(orderService.getById(id));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Integer id){
        orderService.deleteById(id);

        return ResponseEntity.ok().build();
    }

    @Override
    @GetMapping("/user")
    public ResponseEntity<List<Order>> getAllOrdersByUserId(Authentication authentication){
        return ResponseEntity.ok(orderService.getAllOrdersByUserId(authentication));
    }

    @Override
    @GetMapping("/user/{order_id}")
    public ResponseEntity<List<OrderDishResponseDTO>> getAllOrderItems(
            @PathVariable(name="order_id") Integer orderId){
        return ResponseEntity.ok(orderService.getAllOrderItems(orderId));
    }

    @Override
    @PostMapping
    public ResponseEntity<Order> createOrder(Authentication authentication,@Valid @RequestBody OrderDTO orderDTO){
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(orderService.createOrder(authentication, orderDTO.getAddressId()));
    }
}
