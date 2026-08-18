package com.example.foodie.feedback.review.controller;

import com.example.foodie.feedback.review.dto.response.ReviewResponseDTO;
import com.example.foodie.feedback.review.entity.Review;
import com.example.foodie.feedback.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/reviews")
@AllArgsConstructor
public class ReviewController implements ReviewControllerDocs {
    private final ReviewService reviewService;

    @Override
    @GetMapping("/dish/{dish_id}")
    public ResponseEntity<List<ReviewResponseDTO>> getAllReviewsByDishId(
            @PathVariable(name="dish_id") Integer dishId){
        return ResponseEntity.ok(reviewService.findAllReviewsByDishId(dishId));
    }

    @Override
    @PostMapping("/dish/{order_dish_id}")
    public ResponseEntity<Review> addReview(
            @PathVariable(name="order_dish_id") Integer orderDishId,
            @Valid @RequestBody Review review){
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reviewService.addReview(orderDishId, review));
    }
}
