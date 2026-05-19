package com.example.foodie.services.interfaces;

import com.example.foodie.dtos.*;
import com.example.foodie.models.User;

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
