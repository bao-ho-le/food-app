package com.example.foodie.feedback.review.service;

import com.example.foodie.catalog.dish.repository.DishRepository;
import com.example.foodie.ordering.order.entity.OrderDish;
import com.example.foodie.ordering.order.repository.OrderDishRepository;
import com.example.foodie.feedback.review.dto.response.ReviewResponseDTO;
import com.example.foodie.feedback.review.entity.Review;
import com.example.foodie.feedback.review.helper.ReviewHelper;
import com.example.foodie.feedback.review.mapper.ReviewMapper;
import com.example.foodie.feedback.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;
    private final DishRepository dishRepository;
    private final OrderDishRepository orderDishRepository;
    private final ReviewHelper reviewHelper;
    private final ReviewMapper reviewMapper;

    @Override
    public List<ReviewResponseDTO> findAllReviewsByDishId(Integer dishId){
        reviewHelper.validateDishId(dishId);

        dishRepository.findById(dishId)
                .orElseThrow(() -> new RuntimeException("Không tồn tại dish này"));

        return orderDishRepository.findAllByDishId(dishId).stream()
                .filter(orderDish -> orderDish.getReview() != null)
                .map(reviewMapper::toResponse)
                .toList();
    }

    @Override
    public Review addReview(Integer orderDishId, Review review){
        reviewHelper.validateOrderDishId(orderDishId);
        reviewHelper.validateReviewRequest(review);

        OrderDish orderDish = orderDishRepository.findById(orderDishId)
                .orElseThrow(() -> new RuntimeException("order dish không tồn tại"));

        reviewHelper.validateNotReviewed(orderDish);

        Review newReview = reviewMapper.toEntity(review);

        reviewRepository.save(newReview);
        orderDish.setReview(newReview);
        orderDishRepository.save(orderDish);

        return orderDish.getReview();
    }
}
