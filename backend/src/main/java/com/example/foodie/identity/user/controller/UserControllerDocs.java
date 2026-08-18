package com.example.foodie.identity.user.controller;

import com.example.foodie.identity.user.dto.request.ResetPasswordDTO;
import com.example.foodie.identity.user.dto.request.UserDTO;
import com.example.foodie.identity.user.dto.request.UserLoginDTO;
import com.example.foodie.identity.user.dto.request.UserProfileUpdateDTO;
import com.example.foodie.identity.user.dto.response.UserLoginResponseDTO;
import com.example.foodie.identity.user.dto.response.UserProfileDTO;
import com.example.foodie.identity.user.dto.response.UserResponseDTO;
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

@Tag(name = "User", description = "Đăng ký, đăng nhập và quản lý tài khoản người dùng")
public interface UserControllerDocs {

    @Operation(summary = "Đăng ký tài khoản người dùng", description = "Tạo tài khoản người dùng mới. Không yêu cầu đăng nhập.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Đăng ký thành công",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ hoặc email đã tồn tại")
    })
    ResponseEntity<UserResponseDTO> register(@Valid @RequestBody UserDTO userDTO);

    @Operation(summary = "Đăng nhập", description = "Xác thực bằng email/mật khẩu và trả về JWT access token. Không yêu cầu đăng nhập.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Đăng nhập thành công",
                    content = @Content(schema = @Schema(implementation = UserLoginResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Email hoặc mật khẩu không đúng")
    })
    ResponseEntity<UserLoginResponseDTO> login(@Valid @RequestBody UserLoginDTO userLoginDTO);

    @Operation(summary = "Đổi mật khẩu", description = "Đổi mật khẩu của người dùng hiện tại, yêu cầu mật khẩu cũ đúng.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Đổi mật khẩu thành công"),
            @ApiResponse(responseCode = "400", description = "Mật khẩu cũ không đúng hoặc dữ liệu không hợp lệ")
    })
    @SecurityRequirement(name = BEARER_SECURITY_SCHEME)
    ResponseEntity<String> login(@Valid @RequestBody ResetPasswordDTO resetPasswordDTO);

    @Operation(summary = "Lấy thông tin hồ sơ", description = "Trả về hồ sơ của người dùng đang đăng nhập (dựa trên JWT token).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy hồ sơ thành công",
                    content = @Content(schema = @Schema(implementation = UserProfileDTO.class))),
            @ApiResponse(responseCode = "400", description = "Không thể lấy hồ sơ")
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

    void logout();
}
