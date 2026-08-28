package com.example.foodie.auth.helper;

import com.example.foodie.common.exception.ErrorCode;
import com.example.foodie.common.exception.business_exception.IdentityException;
import com.example.foodie.auth.dto.request.ResetPasswordDTO;
import com.example.foodie.auth.dto.request.UserDTO;
import com.example.foodie.auth.dto.request.UserLoginDTO;
import org.springframework.stereotype.Component;

@Component
public class AuthHelper {

    private static final int MIN_PASSWORD_LENGTH = 6;

    public void validateUserRequest(UserDTO userDTO) {
        if (userDTO == null) {
            throw new IdentityException(ErrorCode.USER_REQUEST_REQUIRED);
        }
        if (userDTO.getFullName() == null || userDTO.getFullName().isBlank()) {
            throw new IdentityException(ErrorCode.USER_FULL_NAME_REQUIRED);
        }
        validateEmail(userDTO.getEmail());
        validatePhoneNumber(userDTO.getPhoneNumber());
        validatePassword(userDTO.getPassword());
        if (userDTO.getGender() == null) {
            throw new IdentityException(ErrorCode.USER_GENDER_REQUIRED);
        }
    }

    public void validateLoginRequest(UserLoginDTO userLoginDTO) {
        if (userLoginDTO == null) {
            throw new IdentityException(ErrorCode.USER_LOGIN_REQUEST_REQUIRED);
        }
        validateEmail(userLoginDTO.getEmail());
        if (userLoginDTO.getPassword() == null || userLoginDTO.getPassword().isBlank()) {
            throw new IdentityException(ErrorCode.USER_LOGIN_PASSWORD_REQUIRED);
        }
    }

    public void validateResetPasswordRequest(ResetPasswordDTO resetPasswordDTO) {
        if (resetPasswordDTO == null) {
            throw new IdentityException(ErrorCode.USER_RESET_PASSWORD_REQUEST_REQUIRED);
        }

        if (resetPasswordDTO.getOldPassword() == null || resetPasswordDTO.getOldPassword().isBlank()) {
            throw new IdentityException(ErrorCode.USER_OLD_PASSWORD_REQUIRED);
        }
        validatePassword(resetPasswordDTO.getNewPassword());
    }

    // Trùng với UserHelper#validateEmail/validatePhoneNumber: auth và user là 2 package độc lập,
    // không nên inject helper của nhau chỉ để dùng lại 2 check null/blank ngắn này.
    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IdentityException(ErrorCode.USER_EMAIL_REQUIRED);
        }
    }

    private void validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IdentityException(ErrorCode.USER_PHONE_REQUIRED);
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IdentityException(ErrorCode.USER_PASSWORD_REQUIRED);
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new IdentityException(ErrorCode.USER_PASSWORD_TOO_SHORT);
        }
    }
}
