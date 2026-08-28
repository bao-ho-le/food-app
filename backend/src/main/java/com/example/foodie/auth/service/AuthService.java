package com.example.foodie.auth.service;

import com.example.foodie.auth.dto.request.AdminDTO;
import com.example.foodie.auth.dto.request.ResetPasswordDTO;
import com.example.foodie.auth.dto.request.UserDTO;
import com.example.foodie.auth.dto.request.UserLoginDTO;
import com.example.foodie.auth.dto.response.AdminResponseDTO;
import com.example.foodie.auth.dto.response.UserLoginResponseDTO;
import com.example.foodie.auth.dto.response.UserResponseDTO;
import com.example.foodie.identity.user.entity.User;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    UserResponseDTO register(UserDTO userDTO, HttpServletResponse response);
    AdminResponseDTO registerAdmin(AdminDTO adminDTO, HttpServletResponse response);

    // Tạo user role ADMIN thuần (không phát token/cookie) — dùng cho seed lúc
    // boot (AdminAccountInitializer), nơi không có HttpServletResponse thật.
    User createAdminUser(AdminDTO adminDTO);

    UserLoginResponseDTO login(UserLoginDTO userLoginDTO, HttpServletResponse response);
    void resetPassword(String email, ResetPasswordDTO resetPasswordDTO);

    UserLoginResponseDTO refresh(String refreshTokenStr, HttpServletResponse response);

    void logout(String refreshToken, HttpServletResponse response);
}
