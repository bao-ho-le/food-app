package com.example.foodie.user.service;

import com.example.foodie.user.dto.request.AdminDTO;
import com.example.foodie.user.dto.request.ResetPasswordDTO;
import com.example.foodie.user.dto.request.UserDTO;
import com.example.foodie.user.dto.request.UserLoginDTO;
import com.example.foodie.user.dto.request.UserProfileUpdateDTO;
import com.example.foodie.user.dto.response.AdminResponseDTO;
import com.example.foodie.user.dto.response.UserLoginResponseDTO;
import com.example.foodie.user.dto.response.UserProfileDTO;
import com.example.foodie.user.dto.response.UserResponseDTO;
import com.example.foodie.user.entity.User;

import java.util.List;

import org.springframework.security.core.Authentication;

public interface UserService {
    UserResponseDTO register(UserDTO userDTO);
    UserResponseDTO getUserByEmail(String email);
    AdminResponseDTO registerAdmin(AdminDTO adminDTO);

    UserLoginResponseDTO login(UserLoginDTO userLoginDTO);
    void resetPassword(ResetPasswordDTO resetPasswordDTO);

    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);

    UserProfileDTO getUserProfileByToken(Authentication authentication);
    UserProfileDTO updateProfile(Authentication authentication, UserProfileUpdateDTO userProfileUpdateDTO);

    List<User> getAllUsers(Authentication authentication);
    void blocking(Integer id, Integer type);
}
