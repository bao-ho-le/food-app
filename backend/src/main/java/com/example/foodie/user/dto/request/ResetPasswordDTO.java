package com.example.foodie.user.dto.request;

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
