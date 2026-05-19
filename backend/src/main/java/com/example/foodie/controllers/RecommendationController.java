package com.example.foodie.controllers;


import com.example.foodie.dtos.UpdateUserTagsDTO;
import com.example.foodie.services.interfaces.BiasService;
import com.example.foodie.services.interfaces.RecommendationService;
import com.example.foodie.services.interfaces.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("${api.prefix}/recommendations")
@AllArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final BiasService biasService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<?> recommendations(Authentication authentication) {

        Map<String, Float> result = recommendationService.getRecommendations(authentication);
//        String result = "";

        try{
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(result);

        } catch(RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @PostMapping()
    public ResponseEntity<?> userTags(Authentication authentication, @Valid @RequestBody UpdateUserTagsDTO dto) {

        recommendationService.updateUserTags(authentication, dto);

        try{
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body("update thành công");

        } catch(RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }
}
