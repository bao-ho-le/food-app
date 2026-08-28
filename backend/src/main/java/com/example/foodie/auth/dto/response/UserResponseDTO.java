package com.example.foodie.auth.dto.response;

import com.example.foodie.identity.user.enums.Gender;
import com.example.foodie.identity.user.enums.RoleName;
import lombok.AllArgsConstructor;
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
    private String accessToken;
    private RoleName roleName;
}
