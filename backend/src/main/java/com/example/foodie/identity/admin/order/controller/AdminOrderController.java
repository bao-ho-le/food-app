package com.example.foodie.identity.admin.order.controller;

import com.example.foodie.ordering.order.dto.request.OrderStatusUpdateDTO;
import com.example.foodie.ordering.order.dto.response.OrderDishResponseDTO;
import com.example.foodie.ordering.order.dto.response.OrderResponseDTO;
import com.example.foodie.ordering.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/admin/orders")
@AllArgsConstructor
public class AdminOrderController implements AdminOrderControllerDocs {
    private final OrderService orderService;

    @Override
    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> getAll(){
        return ResponseEntity.ok(orderService.getAll());
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getById(@PathVariable Integer id){
        return ResponseEntity.ok(orderService.getById(id));
    }

    @Override
    @GetMapping("/{id}/items")
    public ResponseEntity<List<OrderDishResponseDTO>> getAllOrderItems(@PathVariable Integer id){
        return ResponseEntity.ok(orderService.getAllOrderItems(id));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Integer id){
        orderService.deleteById(id);

        return ResponseEntity.ok().build();
    }

    @Override
    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponseDTO> updateStatus(@PathVariable Integer id,
                                               @Valid @RequestBody OrderStatusUpdateDTO orderStatusUpdateDTO){
        return ResponseEntity.ok(orderService.updateStatus(id, orderStatusUpdateDTO.getStatus()));
    }
}
