package com.example.foodie.ordering.order.dto.request;

import com.example.foodie.ordering.order.enums.Status;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderStatusUpdateDTO {
    @NotNull(message = "Trạng thái đơn hàng không được để trống")
    private Status status;
}
