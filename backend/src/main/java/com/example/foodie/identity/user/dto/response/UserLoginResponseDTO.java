package com.example.foodie.identity.user.dto.response;

import com.example.foodie.identity.user.enums.RoleName;
import com.example.foodie.identity.user.entity.Role;
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
