package com.example.foodie.dtos;

import com.example.foodie.enums.Action;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserTagsDTO {

    @NotNull
    private int dishId;

    @NotNull
    private Action action;
}
