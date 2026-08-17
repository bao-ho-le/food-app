package com.example.foodie.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Yêu cầu tạo đơn hàng từ giỏ hàng hiện tại")
public class OrderDTO {

    @NotNull
    private Integer addressId;

    // @NotNull
    // private List<Integer> selectedDishes;
}

