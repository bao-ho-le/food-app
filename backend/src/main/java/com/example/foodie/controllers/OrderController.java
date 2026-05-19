package com.example.foodie.controllers;

import com.example.foodie.dtos.OrderDTO;
import com.example.foodie.models.Address;
import com.example.foodie.models.Order;
import com.example.foodie.services.interfaces.AddressService;
import com.example.foodie.services.interfaces.OrderService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/orders")
public class OrderController extends BaseController<Order>{
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        super(orderService);
        this.orderService = orderService;
    }

    @GetMapping("/user")
    public ResponseEntity<?> getAllOrdersByUserId(Authentication authentication){
        try{
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(orderService.getAllOrdersByUserId(authentication));   
        } catch(RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @GetMapping("/user/{order_id}")
    public ResponseEntity<?> getAllOrderItems(@PathVariable(name="order_id") Integer orderId){
        try{
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(orderService.getAllOrderItems(orderId));   
        } catch(RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createOrder(Authentication authentication,@Valid @RequestBody OrderDTO orderDTO){
        try {
            Order newOrder = orderService.createOrder(authentication, orderDTO.getAddressId());

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(newOrder);

        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }
}
