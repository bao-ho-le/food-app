package com.example.foodie.identity.user.controller;

import com.example.foodie.identity.user.dto.request.UserProfileUpdateDTO;
import com.example.foodie.identity.user.dto.response.UserProfileDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestBody;

import static com.example.foodie.common.config.OpenApiConfig.BEARER_SECURITY_SCHEME;

@Tag(name = "User", description = "Quản lý hồ sơ người dùng")
public interface UserControllerDocs {

    @Operation(summary = "Lấy thông tin hồ sơ", description = "Trả về hồ sơ của người dùng đang đăng nhập (dựa trên JWT token).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy hồ sơ thành công",
                    content = @Content(schema = @Schema(implementation = UserProfileDTO.class))),
            @ApiResponse(responseCode = "404", description = "Tài khoản trong token không còn tồn tại")
    })
    @SecurityRequirement(name = BEARER_SECURITY_SCHEME)
    ResponseEntity<UserProfileDTO> getUserProfile(Authentication authentication);

    @Operation(summary = "Cập nhật hồ sơ", description = "Cập nhật thông tin hồ sơ của người dùng đang đăng nhập.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công",
                    content = @Content(schema = @Schema(implementation = UserProfileDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ")
    })
    @SecurityRequirement(name = BEARER_SECURITY_SCHEME)
    ResponseEntity<UserProfileDTO> updateUserProfile(Authentication authentication,
                                                       @Valid @RequestBody UserProfileUpdateDTO userProfileUpdateDTO);
}
