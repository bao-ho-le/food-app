package com.example.foodie.auth.controller;

import com.example.foodie.auth.dto.request.ResetPasswordDTO;
import com.example.foodie.auth.dto.request.UserDTO;
import com.example.foodie.auth.dto.request.UserLoginDTO;
import com.example.foodie.auth.dto.response.UserLoginResponseDTO;
import com.example.foodie.auth.dto.response.UserResponseDTO;
import com.example.foodie.auth.service.AuthService;
import com.example.foodie.common.exception.ErrorCode;
import com.example.foodie.common.exception.business_exception.IdentityException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/users")
@AllArgsConstructor
public class AuthController implements AuthControllerDocs {

    private final AuthService authService;

    @Override
    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody UserDTO userDTO, HttpServletResponse response) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.register(userDTO, response));
    }

    @Override
    @PostMapping("/login")
    public ResponseEntity<UserLoginResponseDTO> login(@Valid @RequestBody UserLoginDTO userLoginDTO, HttpServletResponse response) {
        return ResponseEntity.ok(authService.login(userLoginDTO, response));
    }

    @Override
    @PutMapping("/password")
    public ResponseEntity<String> resetPassword(Authentication authentication, @Valid @RequestBody ResetPasswordDTO resetPasswordDTO){
        authService.resetPassword(authentication.getName(), resetPasswordDTO);

        return ResponseEntity.ok("Đổi mật khẩu thành công");
    }

    @Override
    @PostMapping("/refresh")
    public ResponseEntity<UserLoginResponseDTO> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken == null) {
            throw new IdentityException(ErrorCode.TOKEN_INVALID);
        }
        return ResponseEntity.ok(authService.refresh(refreshToken, response));
    }

    @Override
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {
        authService.logout(refreshToken, response);
        return ResponseEntity.noContent().build();
    }
}
