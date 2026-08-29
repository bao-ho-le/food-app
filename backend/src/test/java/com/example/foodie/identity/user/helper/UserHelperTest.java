package com.example.foodie.identity.user.helper;

import com.example.foodie.common.exception.ErrorCode;
import com.example.foodie.common.exception.business_exception.IdentityException;
import com.example.foodie.identity.user.entity.User;
import com.example.foodie.identity.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

// getUserFromAuthentication được dùng ở mọi ServiceImpl khác (order, address, user-dish,
// review) để suy ra "ai đang gọi API" — nếu nó sai, toàn bộ kiểm tra quyền sở hữu ở các
// service khác sụp đổ theo. EP trên 4 nhánh: chưa đăng nhập (2 dạng) / có tên nhưng
// không tồn tại / tồn tại.
@ExtendWith(MockitoExtension.class)
class UserHelperTest {

    @Mock
    private UserRepository userRepository;

    private UserHelper userHelper;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        userHelper = new UserHelper(userRepository);
    }

    @Test
    @DisplayName("getUserFromAuthentication(null) ném USER_NOT_AUTHENTICATED")
    void should_throwUserNotAuthenticated_when_authenticationIsNull() {
        assertThatThrownBy(() -> userHelper.getUserFromAuthentication(null))
                .isInstanceOf(IdentityException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.USER_NOT_AUTHENTICATED);
    }

    @Test
    @DisplayName("getUserFromAuthentication ném USER_NOT_AUTHENTICATED khi getName() rỗng/blank")
    void should_throwUserNotAuthenticated_when_authenticationNameIsBlank() {
        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
        when(authentication.getName()).thenReturn("   ");

        assertThatThrownBy(() -> userHelper.getUserFromAuthentication(authentication))
                .isInstanceOf(IdentityException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.USER_NOT_AUTHENTICATED);
    }

    @Test
    @DisplayName("getUserFromAuthentication ném USER_NOT_FOUND khi có tên nhưng repository không tìm thấy")
    void should_throwUserNotFound_when_repositoryHasNoMatchingUser() {
        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
        when(authentication.getName()).thenReturn("ghost@test.local");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userHelper.getUserFromAuthentication(authentication))
                .isInstanceOf(IdentityException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("getUserFromAuthentication trả đúng User khi tìm thấy")
    void should_returnUser_when_repositoryFindsMatchingUser() {
        User user = User.builder().id(7).email("real@test.local").build();
        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
        when(authentication.getName()).thenReturn("real@test.local");
        when(userRepository.findByEmail("real@test.local")).thenReturn(Optional.of(user));

        User result = userHelper.getUserFromAuthentication(authentication);

        assertThat(result).isSameAs(user);
    }
}
