package com.example.foodie.auth.service;

import com.example.foodie.auth.dto.request.AdminDTO;
import com.example.foodie.auth.dto.request.ResetPasswordDTO;
import com.example.foodie.auth.dto.request.UserDTO;
import com.example.foodie.auth.dto.request.UserLoginDTO;
import com.example.foodie.auth.dto.response.AdminResponseDTO;
import com.example.foodie.auth.dto.response.UserLoginResponseDTO;
import com.example.foodie.auth.dto.response.UserResponseDTO;

public interface AuthService {
    UserResponseDTO register(UserDTO userDTO);
    AdminResponseDTO registerAdmin(AdminDTO adminDTO);

    UserLoginResponseDTO login(UserLoginDTO userLoginDTO);
    void resetPassword(ResetPasswordDTO resetPasswordDTO);
}
