package com.example.foodie.catalog.dish.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DishStockRequestDTO {

    @NotNull(message = "Số lượng nhập kho không được để trống")
    private Integer quantity;
}
