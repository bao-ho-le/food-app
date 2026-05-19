package com.example.foodie.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserLoginDTO {

    @NotNull
    private String email;

    @NotNull
    private String password;
}
