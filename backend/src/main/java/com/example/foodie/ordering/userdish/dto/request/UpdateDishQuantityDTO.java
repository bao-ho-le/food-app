package com.example.foodie.ordering.userdish.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateDishQuantityDTO {
    @NotNull
    private Integer userDishId;

    @NotNull
    private Integer quantity;
}
