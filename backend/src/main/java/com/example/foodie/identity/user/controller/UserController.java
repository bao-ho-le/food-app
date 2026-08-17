package com.example.foodie.identity.user.controller;

import com.example.foodie.identity.user.dto.request.AdminDTO;
import com.example.foodie.identity.user.dto.request.ResetPasswordDTO;
import com.example.foodie.identity.user.dto.request.UserDTO;
import com.example.foodie.identity.user.dto.request.UserLoginDTO;
import com.example.foodie.identity.user.dto.request.UserProfileUpdateDTO;
import com.example.foodie.identity.user.dto.response.AdminResponseDTO;
import com.example.foodie.identity.user.dto.response.UserLoginResponseDTO;
import com.example.foodie.identity.user.dto.response.UserProfileDTO;
import com.example.foodie.identity.user.dto.response.UserResponseDTO;
import com.example.foodie.identity.user.entity.User;
import com.example.foodie.identity.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import static com.example.foodie.common.config.OpenApiConfig.BEARER_SECURITY_SCHEME;

@RestController
@RequestMapping("${api.prefix}/users")
@AllArgsConstructor
@Tag(name = "User", description = "Đăng ký, đăng nhập và quản lý tài khoản người dùng")
public class UserController {

    /* Đoạn này đang inject interface của UserService, đây là cái nên làm thay vì inject thẳng
       UserServiceImpl, giúp dễ test, dễ thay thế, mở rộng, giả sử tương lai có UserServiceV2 thì chỉ
       cần đổi Bean tại config chứ không đổi ở đây
     */
    private UserService userService;

    @Operation(summary = "Đăng ký tài khoản người dùng", description = "Tạo tài khoản người dùng mới. Không yêu cầu đăng nhập.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Đăng ký thành công",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ hoặc email đã tồn tại")
    })
    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody UserDTO userDTO) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.register(userDTO));
    }

    @PostMapping("/register-admin")
    public ResponseEntity<AdminResponseDTO> registerAdmin(@Valid @RequestBody AdminDTO adminDTO) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.registerAdmin(adminDTO));
    }

    @Operation(summary = "Đăng nhập", description = "Xác thực bằng email/mật khẩu và trả về JWT access token. Không yêu cầu đăng nhập.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Đăng nhập thành công",
                    content = @Content(schema = @Schema(implementation = UserLoginResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Email hoặc mật khẩu không đúng")
    })
    @PostMapping("/login")
    public ResponseEntity<UserLoginResponseDTO> login(@Valid @RequestBody UserLoginDTO userLoginDTO){
        return ResponseEntity.ok(userService.login(userLoginDTO));
    }

    @Operation(summary = "Đổi mật khẩu", description = "Đổi mật khẩu của người dùng hiện tại, yêu cầu mật khẩu cũ đúng.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Đổi mật khẩu thành công"),
            @ApiResponse(responseCode = "400", description = "Mật khẩu cũ không đúng hoặc dữ liệu không hợp lệ")
    })
    @SecurityRequirement(name = BEARER_SECURITY_SCHEME)
    @PutMapping("/password")
    public ResponseEntity<String> login(@Valid @RequestBody ResetPasswordDTO resetPasswordDTO){
        userService.resetPassword(resetPasswordDTO);

        return ResponseEntity.ok("Đổi mật khẩu thành công");
    }

    @Operation(summary = "Lấy thông tin hồ sơ", description = "Trả về hồ sơ của người dùng đang đăng nhập (dựa trên JWT token).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy hồ sơ thành công",
                    content = @Content(schema = @Schema(implementation = UserProfileDTO.class))),
            @ApiResponse(responseCode = "400", description = "Không thể lấy hồ sơ")
    })
    @SecurityRequirement(name = BEARER_SECURITY_SCHEME)
    @GetMapping("/profiles")
    public ResponseEntity<UserProfileDTO> getUserProfile(Authentication authentication){
        return ResponseEntity.ok(userService.getUserProfileByToken(authentication));
    }

    @Operation(summary = "Cập nhật hồ sơ", description = "Cập nhật thông tin hồ sơ của người dùng đang đăng nhập.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công",
                    content = @Content(schema = @Schema(implementation = UserProfileDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ")
    })
    @SecurityRequirement(name = BEARER_SECURITY_SCHEME)
    @PutMapping("/profiles")
    public ResponseEntity<UserProfileDTO> updateUserProfile(Authentication authentication,
                                               @Valid @RequestBody UserProfileUpdateDTO userProfileUpdateDTO){
        return ResponseEntity.ok(userService.updateProfile(authentication, userProfileUpdateDTO));
    }

    @PostMapping("/logout")
    public void logout(){
        System.out.println("Log out");
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<User>> getAllUsers(Authentication authentication){
        return ResponseEntity.ok(userService.getAllUsers(authentication));
    }

    @PostMapping("/blocking/{id}/{type}")
    public ResponseEntity<String> blocking(@PathVariable Integer id, @PathVariable Integer type){
        userService.blocking(id, type);

        return ResponseEntity.ok("Success");
    }
}
