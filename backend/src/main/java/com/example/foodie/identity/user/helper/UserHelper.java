package com.example.foodie.identity.user.helper;

import com.example.foodie.common.exception.ErrorCode;
import com.example.foodie.common.exception.business_exception.IdentityException;
import com.example.foodie.identity.user.dto.request.ResetPasswordDTO;
import com.example.foodie.identity.user.dto.request.UserDTO;
import com.example.foodie.identity.user.dto.request.UserLoginDTO;
import com.example.foodie.identity.user.dto.request.UserProfileUpdateDTO;
import com.example.foodie.identity.user.entity.User;
import com.example.foodie.identity.user.repository.UserRepository;
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
            throw new IdentityException(ErrorCode.USER_NOT_AUTHENTICATED);
        }
    }

    /* Dùng ở nhiều ServiceImpl khác nhau (user, address, order, user-dish)
       nên đặt ở Helper bean thay vì private method của một ServiceImpl */
    public User getUserFromAuthentication(Authentication authentication) {
        validateAuthentication(authentication);

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IdentityException(ErrorCode.USER_NOT_FOUND));
    }

    public void validateUserId(Integer userId) {
        if (userId == null) {
            throw new IdentityException(ErrorCode.USER_ID_REQUIRED);
        }
        if (userId <= 0) {
            throw new IdentityException(ErrorCode.USER_ID_INVALID);
        }
    }

    public void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IdentityException(ErrorCode.USER_EMAIL_REQUIRED);
        }
    }

    public void validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IdentityException(ErrorCode.USER_PHONE_REQUIRED);
        }
    }

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
        validateEmail(resetPasswordDTO.getEmail());
        if (resetPasswordDTO.getOldPassword() == null || resetPasswordDTO.getOldPassword().isBlank()) {
            throw new IdentityException(ErrorCode.USER_OLD_PASSWORD_REQUIRED);
        }
        validatePassword(resetPasswordDTO.getNewPassword());
    }

    public void validateProfileUpdateRequest(UserProfileUpdateDTO userProfileUpdateDTO) {
        if (userProfileUpdateDTO == null) {
            throw new IdentityException(ErrorCode.USER_PROFILE_REQUEST_REQUIRED);
        }
        if (userProfileUpdateDTO.getFullName() == null || userProfileUpdateDTO.getFullName().isBlank()) {
            throw new IdentityException(ErrorCode.USER_FULL_NAME_REQUIRED);
        }
        validateEmail(userProfileUpdateDTO.getEmail());
        validatePhoneNumber(userProfileUpdateDTO.getPhoneNumber());
        if (userProfileUpdateDTO.getGender() == null) {
            throw new IdentityException(ErrorCode.USER_GENDER_REQUIRED);
        }
    }

    public void validateBlockingType(Integer type) {
        if (type == null || (type != 0 && type != 1)) {
            throw new IdentityException(ErrorCode.USER_BLOCK_TYPE_INVALID);
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
