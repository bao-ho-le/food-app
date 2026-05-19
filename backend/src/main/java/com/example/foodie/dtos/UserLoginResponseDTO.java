package com.example.foodie.dtos;

import com.example.foodie.enums.RoleName;
import com.example.foodie.models.Role;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserLoginResponseDTO {

    private String email;
    private RoleName roleName;
    private String token;
}
