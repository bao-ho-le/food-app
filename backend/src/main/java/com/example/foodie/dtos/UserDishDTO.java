package com.example.foodie.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Yêu cầu thêm một món ăn vào giỏ hàng")
public class UserDishDTO {
    @NotNull
    @Min(value = 1, message = "Số lượng phải >= 1")
    private Integer quantity = 1;

    @NotNull
    private Integer dishId;
}
