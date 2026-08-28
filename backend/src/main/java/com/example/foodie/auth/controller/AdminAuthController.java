package com.example.foodie.auth.controller;

import com.example.foodie.auth.dto.request.AdminDTO;
import com.example.foodie.auth.dto.response.AdminResponseDTO;
import com.example.foodie.auth.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Giữ nguyên path "${api.prefix}/admin/users/register-admin" dù controller đã chuyển sang
// package auth, để SecurityConfig (khoá theo prefix "/admin/**") vẫn áp dụng đúng như cũ.
@RestController
@RequestMapping("${api.prefix}/admin/users")
@AllArgsConstructor
public class AdminAuthController implements AdminAuthControllerDocs {

    private final AuthService authService;

    @Override
    @PostMapping("/register-admin")
    public ResponseEntity<AdminResponseDTO> registerAdmin(@Valid @RequestBody AdminDTO adminDTO, HttpServletResponse response) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.registerAdmin(adminDTO, response));
    }
}
