package com.example.foodie.auth.controller;

import com.example.foodie.auth.dto.request.ResetPasswordDTO;
import com.example.foodie.auth.dto.request.UserDTO;
import com.example.foodie.auth.dto.request.UserLoginDTO;
import com.example.foodie.auth.dto.response.UserLoginResponseDTO;
import com.example.foodie.auth.dto.response.UserResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestBody;

import static com.example.foodie.common.config.OpenApiConfig.BEARER_SECURITY_SCHEME;

@Tag(name = "Auth", description = "Đăng ký, đăng nhập và xác thực tài khoản")
public interface AuthControllerDocs {

    @Operation(summary = "Đăng ký tài khoản người dùng", description = "Tạo tài khoản người dùng mới. Không yêu cầu đăng nhập. Refresh token được set qua HttpOnly cookie.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Đăng ký thành công",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ hoặc email đã tồn tại")
    })
    ResponseEntity<UserResponseDTO> register(@Valid @RequestBody UserDTO userDTO, HttpServletResponse response);

    @Operation(summary = "Đăng nhập", description = "Xác thực bằng email/mật khẩu, trả về access token trong body và set refresh token qua HttpOnly cookie. Không yêu cầu đăng nhập.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Đăng nhập thành công",
                    content = @Content(schema = @Schema(implementation = UserLoginResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Email hoặc mật khẩu không đúng")
    })
    ResponseEntity<UserLoginResponseDTO> login(@Valid @RequestBody UserLoginDTO userLoginDTO, HttpServletResponse response);

    @Operation(summary = "Đổi mật khẩu", description = "Đổi mật khẩu của người dùng hiện tại, yêu cầu mật khẩu cũ đúng.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Đổi mật khẩu thành công"),
            @ApiResponse(responseCode = "400", description = "Mật khẩu cũ không đúng hoặc dữ liệu không hợp lệ")
    })
    @SecurityRequirement(name = BEARER_SECURITY_SCHEME)
    ResponseEntity<String> resetPassword(Authentication authentication, @Valid @RequestBody ResetPasswordDTO resetPasswordDTO);


    @Operation(summary = "Làm mới token", description = "Đọc refresh token từ HttpOnly cookie, trả access token mới trong body và rotate refresh token cookie. Không yêu cầu đăng nhập.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Refresh thành công",
                    content = @Content(schema = @Schema(implementation = UserLoginResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Refresh token không hợp lệ, hết hạn, hoặc đã bị dùng lại (reuse detected)")
    })
    ResponseEntity<UserLoginResponseDTO> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response);

    @Operation(summary = "Đăng xuất", description = "Thu hồi refresh token từ cookie và xoá cookie. Luôn trả 204 dù token không tồn tại/đã hết hạn (idempotent). Không yêu cầu đăng nhập.")
    @ApiResponse(responseCode = "204", description = "Đăng xuất thành công (hoặc token đã không còn hiệu lực)")
    ResponseEntity<Void> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response);
}
