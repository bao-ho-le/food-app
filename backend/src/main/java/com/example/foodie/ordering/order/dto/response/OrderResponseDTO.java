package com.example.foodie.ordering.order.dto.response;

import com.example.foodie.ordering.order.enums.Status;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class OrderResponseDTO {
    private Integer id;
    private Integer userId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private Status status;
    private Float totalPrice;
    private String deliveryAddress;
    private Instant createdAt;
    private Instant updatedAt;
}
