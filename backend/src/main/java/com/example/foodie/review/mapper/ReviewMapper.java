package com.example.foodie.review.mapper;

import com.example.foodie.order.entity.OrderDish;
import com.example.foodie.review.dto.response.ReviewResponseDTO;
import com.example.foodie.review.entity.Review;
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
