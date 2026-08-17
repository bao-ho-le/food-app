package com.example.foodie.user.mapper;

import com.example.foodie.user.dto.request.AdminDTO;
import com.example.foodie.user.dto.request.UserDTO;
import com.example.foodie.user.dto.response.AdminResponseDTO;
import com.example.foodie.user.dto.response.UserLoginResponseDTO;
import com.example.foodie.user.dto.response.UserProfileDTO;
import com.example.foodie.user.dto.response.UserResponseDTO;
import com.example.foodie.user.entity.Role;
import com.example.foodie.user.entity.User;
import com.example.foodie.user.enums.RoleName;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(UserDTO userDTO, Role role) {
        return User.builder()
                .fullName(userDTO.getFullName())
                .email(userDTO.getEmail())
                .phoneNumber(userDTO.getPhoneNumber())
                .password(userDTO.getPassword())
                .gender(userDTO.getGender())
                .birthday(userDTO.getBirthday())
                .role(role)
                .build();
    }

    public UserResponseDTO toRegisterResponse(UserDTO userDTO, String token) {
        return UserResponseDTO.builder()
                .fullName(userDTO.getFullName())
                .email(userDTO.getEmail())
                .phoneNumber(userDTO.getPhoneNumber())
                .gender(userDTO.getGender())
                .birthday(userDTO.getBirthday())
                .token(token)
                .roleName(RoleName.USER)
                .build();
    }

    public AdminResponseDTO toAdminRegisterResponse(AdminDTO adminDTO, String token) {
        return AdminResponseDTO.builder()
                .fullName(adminDTO.getFullName())
                .email(adminDTO.getEmail())
                .phoneNumber(adminDTO.getPhoneNumber())
                .gender(adminDTO.getGender())
                .birthday(adminDTO.getBirthday())
                .token(token)
                .build();
    }

    public UserResponseDTO toUserResponse(User user) {
        return UserResponseDTO.builder()
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .gender(user.getGender())
                .birthday(user.getBirthday())
                .build();
    }

    public UserLoginResponseDTO toLoginResponse(User user, String email, String token) {
        return UserLoginResponseDTO.builder()
                .email(email)
                .roleName(user.getRole().getRoleName())
                .token(token)
                .build();
    }

    public UserProfileDTO toProfile(User user) {
        return UserProfileDTO.builder()
                .email(user.getEmail())
                .gender(user.getGender())
                .phoneNumber(user.getPhoneNumber())
                .birthday(user.getBirthday())
                .fullName(user.getFullName())
                .build();
    }
}
