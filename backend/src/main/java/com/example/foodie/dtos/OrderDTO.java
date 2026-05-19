package com.example.foodie.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OrderDTO {

    @NotNull
    private Integer addressId;

    // @NotNull
    // private List<Integer> selectedDishes;
}

