package com.example.foodie.identity.user.helper;

import com.example.foodie.common.exception.ErrorCode;
import com.example.foodie.common.exception.business_exception.IdentityException;
import com.example.foodie.identity.user.dto.request.UserProfileUpdateDTO;
import com.example.foodie.identity.user.entity.User;
import com.example.foodie.identity.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserHelper {

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
}
