package com.example.foodie.identity.user.mapper;

import com.example.foodie.identity.user.dto.response.UserProfileDTO;
import com.example.foodie.identity.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserProfileDTO toProfile(User user) {
        return UserProfileDTO.builder()
                .email(user.getEmail())
                .gender(user.getGender())
                .phoneNumber(user.getPhoneNumber())
                .birthday(user.getBirthday())
                .fullName(user.getFullName())
                .roleName(user.getRole().getRoleName())
                .build();
    }
}
