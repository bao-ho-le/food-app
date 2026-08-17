package com.example.foodie.identity.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Thông tin đăng nhập bằng email và mật khẩu")
public class UserLoginDTO {

    @NotNull
    private String email;

    @NotNull
    private String password;
}
