package com.example.foodie.review.service;

import com.example.foodie.review.dto.response.ReviewResponseDTO;
import com.example.foodie.review.entity.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewService {
    List<ReviewResponseDTO> findAllReviewsByDishId(Integer dishId);
    Review addReview(Integer orderDishId, Review review);
}
