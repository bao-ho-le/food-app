package com.example.foodie.feedback.review.mapper;

import com.example.foodie.ordering.order.entity.OrderDish;
import com.example.foodie.feedback.review.dto.response.ReviewResponseDTO;
import com.example.foodie.feedback.review.entity.Review;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    public Review toEntity(Review review) {
        return Review.builder()
                .comment(review.getComment())
                .rating(review.getRating())
                .build();
    }

    public ReviewResponseDTO toResponse(OrderDish orderDish) {
        Review review = orderDish.getReview();

        return new ReviewResponseDTO(
                orderDish.getOrder().getUser().getFullName(),
                review.getComment(),
                review.getRating(),
                review.getCreatedAt()
        );
    }
}
