package com.example.foodie.services.interfaces;

import com.example.foodie.dtos.UpdateUserTagsDTO;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;

public interface RecommendationService {
    Map<String, Float> getRecommendations(Authentication authentication);
    void updateUserTags(Authentication authentication, UpdateUserTagsDTO dto);
}
