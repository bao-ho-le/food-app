package com.example.foodie.identity.admin.order.controller;

import com.example.foodie.ordering.order.dto.request.OrderStatusUpdateDTO;
import com.example.foodie.ordering.order.dto.response.OrderDishResponseDTO;
import com.example.foodie.ordering.order.entity.Order;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

import static com.example.foodie.common.config.OpenApiConfig.BEARER_SECURITY_SCHEME;

@Tag(name = "Admin - Order", description = "Quản trị đơn hàng (chỉ ADMIN)")
@SecurityRequirement(name = BEARER_SECURITY_SCHEME)
public interface AdminOrderControllerDocs {

    ResponseEntity<List<Order>> getAll();

    ResponseEntity<Order> getById(@PathVariable Integer id);

    ResponseEntity<List<OrderDishResponseDTO>> getAllOrderItems(@PathVariable Integer id);

    ResponseEntity<Void> deleteById(@PathVariable Integer id);

    ResponseEntity<Order> updateStatus(@PathVariable Integer id, @Valid @RequestBody OrderStatusUpdateDTO orderStatusUpdateDTO);
}
