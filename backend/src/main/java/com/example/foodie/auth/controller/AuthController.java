package com.example.foodie.auth.controller;

import com.example.foodie.auth.dto.request.ResetPasswordDTO;
import com.example.foodie.auth.dto.request.UserDTO;
import com.example.foodie.auth.dto.request.UserLoginDTO;
import com.example.foodie.auth.dto.response.UserLoginResponseDTO;
import com.example.foodie.auth.dto.response.UserResponseDTO;
import com.example.foodie.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/users")
@AllArgsConstructor
public class AuthController implements AuthControllerDocs {

    private final AuthService authService;

    @Override
    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody UserDTO userDTO) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.register(userDTO));
    }

    @Override
    @PostMapping("/login")
    public ResponseEntity<UserLoginResponseDTO> login(@Valid @RequestBody UserLoginDTO userLoginDTO){
        return ResponseEntity.ok(authService.login(userLoginDTO));
    }

    @Override
    @PutMapping("/password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordDTO resetPasswordDTO){
        authService.resetPassword(resetPasswordDTO);

        return ResponseEntity.ok("Đổi mật khẩu thành công");
    }

    @Override
    @PostMapping("/logout")
    public void logout(){
        System.out.println("Log out");
    }
}
