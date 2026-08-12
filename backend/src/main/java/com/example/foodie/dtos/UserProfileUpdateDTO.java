package com.example.foodie.dtos;

import com.example.foodie.enums.Gender;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "Thông tin cập nhật hồ sơ người dùng")
public class UserProfileUpdateDTO {
    @NotNull(message = "Tên không được để trống")
    private String fullName;

    @Past(message = "Ngày sinh phải trong quá khứ")
    @JsonFormat(pattern="dd-MM-yyyy")
    private LocalDate birthday;

    @NotNull(message = "Giới tính không được để trống")
    private Gender gender;

    @NotNull(message = "Số điện thoại không được để trống")
    @Pattern(
            regexp = "^(0|\\+84)(\\d{9})$",
            message = "Số điện thoại không hợp lệ (phải bắt đầu bằng 0 hoặc +84 và có 10 số)"
    )
    private String phoneNumber;

    @NotNull(message = "Email không được để trống")
    @Email(message = "Email sai định dạng")
    private String email;
}
