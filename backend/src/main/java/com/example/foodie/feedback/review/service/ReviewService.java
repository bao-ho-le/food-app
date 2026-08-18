package com.example.foodie.feedback.review.service;

import com.example.foodie.feedback.review.dto.response.ReviewResponseDTO;
import com.example.foodie.feedback.review.entity.Review;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

public interface ReviewService {
    List<ReviewResponseDTO> findAllReviewsByDishId(Integer dishId);
    Review addReview(Authentication authentication, Integer orderDishId, Review review);
}
