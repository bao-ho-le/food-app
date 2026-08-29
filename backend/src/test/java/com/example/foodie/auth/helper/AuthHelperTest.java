package com.example.foodie.auth.helper;

import com.example.foodie.auth.dto.request.ResetPasswordDTO;
import com.example.foodie.auth.dto.request.UserDTO;
import com.example.foodie.common.exception.ErrorCode;
import com.example.foodie.common.exception.business_exception.IdentityException;
import com.example.foodie.identity.user.enums.Gender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Chỉ test biên có ý nghĩa nghiệp vụ (BVA quanh ngưỡng mật khẩu 6 ký tự) và các
// DTO null-guard ở tầng đầu vào. Các nhánh null/blank còn lại (fullName, email,
// phone, gender) là Low risk, phủ ở Phase 3 qua Bean Validation tầng HTTP.
class AuthHelperTest {

    private final AuthHelper authHelper = new AuthHelper();

    private static UserDTO validUserDto(String password) {
        UserDTO dto = new UserDTO();
        dto.setFullName("Nguyen Van A");
        dto.setGender(Gender.MALE);
        dto.setPhoneNumber("0912345678");
        dto.setEmail("a@b.com");
        dto.setPassword(password);
        return dto;
    }

    @Test
    @DisplayName("password 5 ký tự ném USER_PASSWORD_TOO_SHORT")
    void should_throwPasswordTooShort_when_passwordHasFiveChars() {
        assertThatThrownBy(() -> authHelper.validateUserRequest(validUserDto("12345")))
                .isInstanceOf(IdentityException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.USER_PASSWORD_TOO_SHORT);
    }

    @Test
    @DisplayName("password 6 ký tự không ném — biên dưới hợp lệ")
    void should_notThrow_when_passwordHasSixChars() {
        assertThatCode(() -> authHelper.validateUserRequest(validUserDto("123456")))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateUserRequest(null) ném USER_REQUEST_REQUIRED")
    void should_throwUserRequestRequired_when_userDtoIsNull() {
        assertThatThrownBy(() -> authHelper.validateUserRequest(null))
                .isInstanceOf(IdentityException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.USER_REQUEST_REQUIRED);
    }

    @Test
    @DisplayName("validateLoginRequest(null) ném USER_LOGIN_REQUEST_REQUIRED")
    void should_throwUserLoginRequestRequired_when_loginDtoIsNull() {
        assertThatThrownBy(() -> authHelper.validateLoginRequest(null))
                .isInstanceOf(IdentityException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.USER_LOGIN_REQUEST_REQUIRED);
    }

    @Test
    @DisplayName("ResetPasswordDTO với newPassword 5 ký tự ném USER_PASSWORD_TOO_SHORT")
    void should_throwPasswordTooShort_when_newPasswordHasFiveChars() {
        ResetPasswordDTO dto = new ResetPasswordDTO();
        dto.setOldPassword("123456");
        dto.setNewPassword("12345");

        assertThatThrownBy(() -> authHelper.validateResetPasswordRequest(dto))
                .isInstanceOf(IdentityException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.USER_PASSWORD_TOO_SHORT);
    }
}
