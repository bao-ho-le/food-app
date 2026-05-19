package com.example.foodie.dtos;

import com.example.foodie.enums.Gender;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserDTO {
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

    @NotNull(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password;
}
