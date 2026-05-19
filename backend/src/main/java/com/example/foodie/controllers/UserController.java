package com.example.foodie.controllers;

import com.example.foodie.dtos.*;
import com.example.foodie.models.User;
import com.example.foodie.services.interfaces.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/users")
@AllArgsConstructor
public class UserController {

    /* Đoạn này đang inject interface của UserService, đây là cái nên làm thay vì inject thẳng
       UserServiceImpl, giúp dễ test, dễ thay thế, mở rộng, giả sử tương lai có UserServiceV2 thì chỉ
       cần đổi Bean tại config chứ không đổi ở đây
     */
    private UserService userService;

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
