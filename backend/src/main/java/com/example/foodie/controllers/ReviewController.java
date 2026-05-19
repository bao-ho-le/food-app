package com.example.foodie.controllers;

import com.example.foodie.dtos.ReviewResponseDTO;
import com.example.foodie.models.Address;
import com.example.foodie.models.Restaurant;
import com.example.foodie.models.Review;
import com.example.foodie.services.interfaces.ReviewService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/reviews")
@AllArgsConstructor
public class ReviewController {
    private ReviewService reviewService;

    @GetMapping("/dish/{dish_id}")
    public ResponseEntity<?> getAllReviewsByDishId(@PathVariable(name="dish_id") Integer dishId){
        List<ReviewResponseDTO> reviews = reviewService.findAllReviewsByDishId(dishId);

        try{
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(reviews);

        } catch(RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/dish/{order_dish_id}")
    public ResponseEntity<?> addReview(@PathVariable(name="order_dish_id") Integer orderDishId, @Valid @RequestBody Review review){
        try {
            Review newReview = reviewService.addReview(orderDishId, review);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(newReview);
        }catch(Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }
}
