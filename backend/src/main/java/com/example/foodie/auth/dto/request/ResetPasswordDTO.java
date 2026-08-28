package com.example.foodie.auth.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ResetPasswordDTO {

    @NotNull
    private String newPassword;

    @NotNull
    private String oldPassword;
}
