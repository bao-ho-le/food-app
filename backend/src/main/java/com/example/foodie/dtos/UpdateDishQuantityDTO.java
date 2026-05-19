package com.example.foodie.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateDishQuantityDTO {
    @NotNull
    private Integer userDishId;

    @NotNull
    private Integer quantity;
}
