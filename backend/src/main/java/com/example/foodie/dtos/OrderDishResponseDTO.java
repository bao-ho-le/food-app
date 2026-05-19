package com.example.foodie.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderDishResponseDTO {
    
    @NotNull
    private Integer id;

    @NotNull    
    private Integer dishId;

    @NotNull
    private String dishName;

    @NotNull
    private Integer quantity;

    @NotNull
    private Float price;

    @NotNull
    private String imageUrl;

    @NotNull
    private String restaurantName;

    @NotNull
    private Boolean reviewed;
}
