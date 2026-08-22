package com.example.foodie.identity.user.service;

import com.example.foodie.identity.user.dto.request.UserProfileUpdateDTO;
import com.example.foodie.identity.user.dto.response.UserProfileDTO;
import com.example.foodie.identity.user.entity.User;

import java.util.List;

import org.springframework.security.core.Authentication;

public interface UserService {
    UserProfileDTO getUserByEmail(String email);

    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);

    UserProfileDTO getUserProfileByToken(Authentication authentication);
    UserProfileDTO updateProfile(Authentication authentication, UserProfileUpdateDTO userProfileUpdateDTO);

    List<User> getAllUsers();
    void blocking(Integer id, Integer type);
}
