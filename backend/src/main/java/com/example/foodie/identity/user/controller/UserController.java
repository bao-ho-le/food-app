package com.example.foodie.identity.user.controller;

import com.example.foodie.identity.user.dto.request.UserProfileUpdateDTO;
import com.example.foodie.identity.user.dto.response.UserProfileDTO;
import com.example.foodie.identity.user.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/users")
@AllArgsConstructor
public class UserController implements UserControllerDocs {

    /* Đoạn này đang inject interface của UserService, đây là cái nên làm thay vì inject thẳng
       UserServiceImpl, giúp dễ test, dễ thay thế, mở rộng, giả sử tương lai có UserServiceV2 thì chỉ
       cần đổi Bean tại config chứ không đổi ở đây
     */
    private final UserService userService;

    @Override
    @GetMapping("/profiles")
    public ResponseEntity<UserProfileDTO> getUserProfile(Authentication authentication){
        return ResponseEntity.ok(userService.getUserProfileByToken(authentication));
    }

    @Override
    @PutMapping("/profiles")
    public ResponseEntity<UserProfileDTO> updateUserProfile(Authentication authentication,
                                               @Valid @RequestBody UserProfileUpdateDTO userProfileUpdateDTO){
        return ResponseEntity.ok(userService.updateProfile(authentication, userProfileUpdateDTO));
    }
}
