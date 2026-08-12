package com.example.foodie.dtos;

import com.example.foodie.enums.RoleName;
import com.example.foodie.models.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@Schema(description = "Kết quả đăng nhập gồm JWT access token")
public class UserLoginResponseDTO {

    private String email;
    private RoleName roleName;
    private String token;
}
