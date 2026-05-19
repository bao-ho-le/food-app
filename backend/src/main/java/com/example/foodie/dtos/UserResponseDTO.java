package com.example.foodie.dtos;

import com.example.foodie.enums.Gender;
import com.example.foodie.enums.RoleName;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    // Phần response không cần kiểm tra ràng buộc
    private String fullName;
    private LocalDate birthday;
    private Gender gender;
    private String phoneNumber;
    private String email;
    private String token;
    private RoleName roleName;

    public static UserResponseDTO createUserResponseFromDTO(UserDTO userDTO, String token){
        return  UserResponseDTO.builder()
                .fullName(userDTO.getFullName())
                .email(userDTO.getEmail())
                .phoneNumber(userDTO.getPhoneNumber())
                .gender(userDTO.getGender())
                .birthday(userDTO.getBirthday())
                .token(token)
                .roleName(RoleName.USER)
                .build();
    }
}
