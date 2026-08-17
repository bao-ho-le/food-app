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
    public ResponseEntity<?> register(@Valid @RequestBody UserDTO userDTO) {
        try {
            UserResponseDTO userResponseDTO = userService.register(userDTO);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(userResponseDTO);

        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/register-admin")
    public ResponseEntity<?> registerAdmin(@Valid @RequestBody AdminDTO adminDTO) {
        try {
            AdminResponseDTO adminResponseDTO = userService.registerAdmin(adminDTO);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(adminResponseDTO);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @Operation(summary = "Đăng nhập", description = "Xác thực bằng email/mật khẩu và trả về JWT access token. Không yêu cầu đăng nhập.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Đăng nhập thành công",
                    content = @Content(schema = @Schema(implementation = UserLoginResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Email hoặc mật khẩu không đúng")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserLoginDTO userLoginDTO){
        try{
            UserLoginResponseDTO userLoginResponseDTO = userService.login(userLoginDTO);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(userLoginResponseDTO);
        } catch (Exception e){
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(e.getMessage());
        }
    }

    @Operation(summary = "Đổi mật khẩu", description = "Đổi mật khẩu của người dùng hiện tại, yêu cầu mật khẩu cũ đúng.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Đổi mật khẩu thành công"),
            @ApiResponse(responseCode = "400", description = "Mật khẩu cũ không đúng hoặc dữ liệu không hợp lệ")
    })
    @SecurityRequirement(name = BEARER_SECURITY_SCHEME)
    @PutMapping("/password")
    public ResponseEntity<?> login(@Valid @RequestBody ResetPasswordDTO resetPasswordDTO){
        try {
            userService.resetPassword(resetPasswordDTO);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body("Đổi mật khẩu thành công");
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @Operation(summary = "Lấy thông tin hồ sơ", description = "Trả về hồ sơ của người dùng đang đăng nhập (dựa trên JWT token).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy hồ sơ thành công",
                    content = @Content(schema = @Schema(implementation = UserProfileDTO.class))),
            @ApiResponse(responseCode = "400", description = "Không thể lấy hồ sơ")
    })
    @SecurityRequirement(name = BEARER_SECURITY_SCHEME)
    @GetMapping("/profiles")
    public ResponseEntity<?> getUserProfile(Authentication authentication){
        try {
            UserProfileDTO userProfile = userService.getUserProfileByToken(authentication);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(userProfile);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @Operation(summary = "Cập nhật hồ sơ", description = "Cập nhật thông tin hồ sơ của người dùng đang đăng nhập.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công",
                    content = @Content(schema = @Schema(implementation = UserProfileDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ")
    })
    @SecurityRequirement(name = BEARER_SECURITY_SCHEME)
    @PutMapping("/profiles")
    public ResponseEntity<?> updateUserProfile(Authentication authentication,
                                               @Valid @RequestBody UserProfileUpdateDTO userProfileUpdateDTO){
        try {
            UserProfileDTO userProfile = userService.updateProfile(authentication, userProfileUpdateDTO);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(userProfile);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public void logout(){
        System.out.println("Log out");
    }

    @GetMapping("/getAll")
    public ResponseEntity<?> getAllUsers(Authentication authentication){
        try {
            List<User> allUsers = userService.getAllUsers(authentication);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(allUsers);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/blocking/{id}/{type}")
    public ResponseEntity<?> blocking(@PathVariable Integer id, @PathVariable Integer type){
        try{
           userService.blocking(id, type);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body("Success");
        }catch(Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }
}
