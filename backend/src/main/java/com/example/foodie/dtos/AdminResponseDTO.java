package com.example.foodie.dtos;

import com.example.foodie.models.Role;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
public class AdminResponseDTO extends UserResponseDTO{

    public static AdminResponseDTO createAdminResponseFromDTO(AdminDTO adminDTO, String token){
        return  AdminResponseDTO.builder()
                .fullName(adminDTO.getFullName())
                .email(adminDTO.getEmail())
                .phoneNumber(adminDTO.getPhoneNumber())
                .gender(adminDTO.getGender())
                .birthday(adminDTO.getBirthday())
                .token(token)
                .build();
    }
}
