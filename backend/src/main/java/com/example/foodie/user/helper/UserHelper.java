package com.example.foodie.user.helper;

import com.example.foodie.user.dto.request.ResetPasswordDTO;
import com.example.foodie.user.dto.request.UserDTO;
import com.example.foodie.user.dto.request.UserLoginDTO;
import com.example.foodie.user.dto.request.UserProfileUpdateDTO;
import com.example.foodie.user.entity.User;
import com.example.foodie.user.enums.RoleName;
import com.example.foodie.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserHelper {

    private static final int MIN_PASSWORD_LENGTH = 6;

    private final UserRepository userRepository;

    public void validateAuthentication(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new RuntimeException("Chưa đăng nhập");
        }
    }

    /* Dùng ở nhiều ServiceImpl khác nhau (user, address, order, user-dish)
       nên đặt ở Helper bean thay vì private method của một ServiceImpl */
    public User getUserFromAuthentication(Authentication authentication) {
        validateAuthentication(authentication);

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
    }

    public void validateUserId(Integer userId) {
        if (userId == null || userId <= 0) {
            throw new RuntimeException("Id user không hợp lệ");
        }
    }

    public void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Email không được để trống");
        }
    }

    public void validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new RuntimeException("Số điện thoại không được để trống");
        }
    }

    public void validateUserRequest(UserDTO userDTO) {
        if (userDTO == null) {
            throw new RuntimeException("Thông tin đăng ký không được để trống");
        }
        if (userDTO.getFullName() == null || userDTO.getFullName().isBlank()) {
            throw new RuntimeException("Tên không được để trống");
        }
        validateEmail(userDTO.getEmail());
        validatePhoneNumber(userDTO.getPhoneNumber());
        validatePassword(userDTO.getPassword());
        if (userDTO.getGender() == null) {
            throw new RuntimeException("Giới tính không được để trống");
        }
    }

    public void validateLoginRequest(UserLoginDTO userLoginDTO) {
        if (userLoginDTO == null) {
            throw new RuntimeException("Thông tin đăng nhập không được để trống");
        }
        validateEmail(userLoginDTO.getEmail());
        if (userLoginDTO.getPassword() == null || userLoginDTO.getPassword().isBlank()) {
            throw new RuntimeException("Mật khẩu không được để trống");
        }
    }

    public void validateResetPasswordRequest(ResetPasswordDTO resetPasswordDTO) {
        if (resetPasswordDTO == null) {
            throw new RuntimeException("Thông tin đổi mật khẩu không được để trống");
        }
        validateEmail(resetPasswordDTO.getEmail());
        if (resetPasswordDTO.getOldPassword() == null || resetPasswordDTO.getOldPassword().isBlank()) {
            throw new RuntimeException("Mật khẩu cũ không được để trống");
        }
        validatePassword(resetPasswordDTO.getNewPassword());
    }

    public void validateProfileUpdateRequest(UserProfileUpdateDTO userProfileUpdateDTO) {
        if (userProfileUpdateDTO == null) {
            throw new RuntimeException("Thông tin hồ sơ không được để trống");
        }
        if (userProfileUpdateDTO.getFullName() == null || userProfileUpdateDTO.getFullName().isBlank()) {
            throw new RuntimeException("Tên không được để trống");
        }
        validateEmail(userProfileUpdateDTO.getEmail());
        validatePhoneNumber(userProfileUpdateDTO.getPhoneNumber());
        if (userProfileUpdateDTO.getGender() == null) {
            throw new RuntimeException("Giới tính không được để trống");
        }
    }

    public void validateBlockingType(Integer type) {
        if (type == null || (type != 0 && type != 1)) {
            throw new RuntimeException("Loại không hợp lệ");
        }
    }

    public void validateAdminRole(User user) {
        if (user.getRole().getRoleName() != RoleName.ADMIN) {
            throw new RuntimeException("Chỉ admin mới có quyền truy cập danh sách người dùng");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new RuntimeException("Mật khẩu không được để trống");
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new RuntimeException("Mật khẩu phải có ít nhất " + MIN_PASSWORD_LENGTH + " ký tự");
        }
    }
}
