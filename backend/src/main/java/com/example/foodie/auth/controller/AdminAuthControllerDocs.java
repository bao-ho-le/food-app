package com.example.foodie.auth.controller;

import com.example.foodie.auth.dto.request.AdminDTO;
import com.example.foodie.auth.dto.response.AdminResponseDTO;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import static com.example.foodie.common.config.OpenApiConfig.BEARER_SECURITY_SCHEME;

@Tag(name = "Admin - Auth", description = "Đăng ký tài khoản admin")
@SecurityRequirement(name = BEARER_SECURITY_SCHEME)
public interface AdminAuthControllerDocs {

    ResponseEntity<AdminResponseDTO> registerAdmin(@Valid @RequestBody AdminDTO adminDTO);
}
