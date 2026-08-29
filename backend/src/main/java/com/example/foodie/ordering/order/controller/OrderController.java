package com.example.foodie.ordering.order.controller;

import com.example.foodie.ordering.order.dto.request.OrderDTO;
import com.example.foodie.ordering.order.dto.response.OrderDishResponseDTO;
import com.example.foodie.ordering.order.dto.response.OrderResponseDTO;
import com.example.foodie.ordering.order.service.OrderIdempotencyService;
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
    private final OrderIdempotencyService orderIdempotencyService;

    @Override
    @GetMapping("/user")
    public ResponseEntity<List<OrderResponseDTO>> getAllOrdersByUserId(Authentication authentication){
        return ResponseEntity.ok(orderService.getAllOrdersByUserId(authentication));
    }

    @Override
    @GetMapping("/user/{order_id}")
    public ResponseEntity<List<OrderDishResponseDTO>> getAllOrderItems(
            Authentication authentication,
            @PathVariable(name="order_id") Integer orderId){
        return ResponseEntity.ok(orderService.getOwnOrderItems(authentication, orderId));
    }

    @Override
    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(
            Authentication authentication,
            @Valid @RequestBody OrderDTO orderDTO,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ){
        return orderIdempotencyService.createOrder(authentication, orderDTO, idempotencyKey);
    }

    @Override
    @PatchMapping("/user/{id}/cancel")
    public ResponseEntity<OrderResponseDTO> cancelMyOrder(Authentication authentication, @PathVariable Integer id) {
        return ResponseEntity.ok(orderService.cancelOwnOrder(authentication, id));
    }

    @Override
    @PatchMapping("/user/{id}/confirm-received")
    public ResponseEntity<OrderResponseDTO> confirmMyOrderReceived(Authentication authentication, @PathVariable Integer id) {
        return ResponseEntity.ok(orderService.confirmOwnOrderReceived(authentication, id));
    }
}
