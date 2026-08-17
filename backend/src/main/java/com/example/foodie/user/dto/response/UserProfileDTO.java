package com.example.foodie.user.dto.response;

import com.example.foodie.user.enums.Gender;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
@Schema(description = "Hồ sơ của người dùng đang đăng nhập")
public class UserProfileDTO {
    private String fullName;
    private LocalDate birthday;
    private Gender gender;
    private String phoneNumber;
    private String email;
}
