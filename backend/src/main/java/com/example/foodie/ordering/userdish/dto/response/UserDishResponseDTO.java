package com.example.foodie.ordering.userdish.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Một mục trong giỏ hàng của người dùng")
public class UserDishResponseDTO {
    private Integer id;
    private Integer quantity;
    private DishSummaryDTO dish;
}
