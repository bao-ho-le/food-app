package com.example.foodie.auth.security;

import com.example.foodie.identity.user.entity.Role;
import com.example.foodie.identity.user.entity.User;
import com.example.foodie.identity.user.enums.Gender;
import com.example.foodie.identity.user.enums.RoleName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static org.assertj.core.api.Assertions.assertThat;

// isEnabled() quyết định tài khoản bị khoá có đăng nhập được hay không dù token
// còn hạn. Prefix "ROLE_" là thứ khiến hasRole("ADMIN") trong SecurityConfig hoạt
// động — sai prefix thì toàn bộ phân quyền admin sập, nên test khẳng định rõ chuỗi
// authority thay vì chỉ so sánh roleName.
class CustomUserDetailsTest {

    private static User userWithRole(RoleName roleName, boolean active) {
        Role role = new Role();
        role.setRoleName(roleName);
        return User.builder()
                .email("a@b.com")
                .gender(Gender.MALE)
                .role(role)
                .isActive(active)
                .build();
    }

    @Test
    @DisplayName("Role USER cho đúng một authority ROLE_USER")
    void should_returnRoleUserAuthority_when_userHasUserRole() {
        CustomUserDetails details = new CustomUserDetails(userWithRole(RoleName.USER, true));

        assertThat((Iterable<GrantedAuthority>) details.getAuthorities())
                .containsExactly(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Test
    @DisplayName("Role ADMIN cho đúng một authority ROLE_ADMIN")
    void should_returnRoleAdminAuthority_when_userHasAdminRole() {
        CustomUserDetails details = new CustomUserDetails(userWithRole(RoleName.ADMIN, true));

        assertThat((Iterable<GrantedAuthority>) details.getAuthorities())
                .containsExactly(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("isEnabled() phản ánh đúng user.isActive() — true và false")
    void should_reflectUserActiveFlag_when_checkingIsEnabled() {
        assertThat(new CustomUserDetails(userWithRole(RoleName.USER, true)).isEnabled()).isTrue();
        assertThat(new CustomUserDetails(userWithRole(RoleName.USER, false)).isEnabled()).isFalse();
    }

    @Test
    @DisplayName("getUsername() trả về email — Spring Security dùng email làm username")
    void should_returnEmailAsUsername_when_gettingUsername() {
        CustomUserDetails details = new CustomUserDetails(userWithRole(RoleName.USER, true));

        assertThat(details.getUsername()).isEqualTo("a@b.com");
    }
}
