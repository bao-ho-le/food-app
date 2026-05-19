package com.example.foodie.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserDishDTO {
    @NotNull
    @Min(value = 1, message = "Số lượng phải >= 1")
    private Integer quantity = 1;

    @NotNull
    private Integer dishId;
}
