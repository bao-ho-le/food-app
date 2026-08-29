package com.example.foodie.identity.user.service;

import com.example.foodie.auth.repository.RefreshTokenRepository;
import com.example.foodie.common.exception.ErrorCode;
import com.example.foodie.common.exception.business_exception.IdentityException;
import com.example.foodie.identity.user.dto.request.UserProfileUpdateDTO;
import com.example.foodie.identity.user.dto.response.UserProfileDTO;
import com.example.foodie.identity.user.entity.Role;
import com.example.foodie.identity.user.entity.User;
import com.example.foodie.identity.user.enums.Gender;
import com.example.foodie.identity.user.enums.RoleName;
import com.example.foodie.identity.user.helper.UserHelper;
import com.example.foodie.identity.user.mapper.UserMapper;
import com.example.foodie.identity.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private Authentication authentication;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        UserHelper userHelper = new UserHelper(userRepository);
        userService = new UserServiceImpl(userRepository, userHelper, new UserMapper(), refreshTokenRepository);
    }

    private static User userWithId(int id) {
        Role role = new Role();
        role.setId(1);
        role.setRoleName(RoleName.USER);
        return User.builder().id(id).email("u" + id + "@test.local").role(role).isActive(true).build();
    }

    // Khoá tài khoản mà không chấm dứt phiên đang hoạt động thì việc khoá vô nghĩa --
    // người bị khoá vẫn dùng được tới khi token hết hạn. Đây là verify() chính đáng
    // (Quy tắc 3, trường hợp đếm số lần / khẳng định một hành động nghiệp vụ phải xảy ra).
    // Test này KHÔNG chứng minh việc thu hồi thực sự commit xuống DB -- đó thuộc Phase 5,
    // xem cảnh báo BUG-001 trong RefreshTokenReuseDetectionTest về @Transactional rollback.
    @Test
    @DisplayName("blocking(type=0) khoá tài khoản và thu hồi toàn bộ refresh token")
    void should_deactivateAndRevokeAllTokens_when_blockingTypeIsZero() {
        User user = userWithId(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        userService.blocking(1, 0);

        assertThat(user.isActive()).isFalse();
        verify(refreshTokenRepository).revokeAllByUser(org.mockito.ArgumentMatchers.eq(user), any());
    }

    @Test
    @DisplayName("blocking(type=1) mở khoá tài khoản và không thu hồi token nào")
    void should_activateWithoutRevokingTokens_when_blockingTypeIsOne() {
        User user = userWithId(1);
        user.setActive(false);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        userService.blocking(1, 1);

        assertThat(user.isActive()).isTrue();
        verify(refreshTokenRepository, never()).revokeAllByUser(any(), any());
    }

    @Test
    @DisplayName("blocking user không tồn tại ném USER_NOT_FOUND")
    void should_throwUserNotFound_when_userDoesNotExist() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.blocking(99, 0))
                .isInstanceOf(IdentityException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("updateProfile hợp lệ ghi đúng 5 trường fullName/email/phoneNumber/gender/birthday")
    void should_updateAllFiveFields_when_profileRequestIsValid() {
        User user = userWithId(1);
        when(authentication.getName()).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserProfileUpdateDTO request = new UserProfileUpdateDTO();
        request.setFullName("Nguyen Van A");
        request.setEmail("new-email@test.local");
        request.setPhoneNumber("0987654321");
        request.setGender(Gender.MALE);
        request.setBirthday(LocalDate.of(2000, 1, 1));

        UserProfileDTO result = userService.updateProfile(authentication, request);

        assertThat(result.getFullName()).isEqualTo("Nguyen Van A");
        assertThat(result.getEmail()).isEqualTo("new-email@test.local");
        assertThat(result.getPhoneNumber()).isEqualTo("0987654321");
        assertThat(result.getGender()).isEqualTo(Gender.MALE);
        assertThat(result.getBirthday()).isEqualTo(LocalDate.of(2000, 1, 1));
    }
}
