package com.example.foodie.services.interfaces;

import com.example.foodie.dtos.ReviewResponseDTO;
import com.example.foodie.models.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewService {
    List<ReviewResponseDTO> findAllReviewsByDishId(Integer dishId);
    Review addReview(Integer orderDishId, Review review);
}
