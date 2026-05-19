package com.example.foodie.dtos;

import com.example.foodie.enums.Gender;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class UserProfileDTO {
    private String fullName;
    private LocalDate birthday;
    private Gender gender;
    private String phoneNumber;
    private String email;
}
