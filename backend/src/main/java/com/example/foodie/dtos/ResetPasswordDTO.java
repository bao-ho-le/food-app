package com.example.foodie.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ResetPasswordDTO {
    @NotNull
    private String email;

    @NotNull
    private String newPassword;

    @NotNull
    private String oldPassword;
}
