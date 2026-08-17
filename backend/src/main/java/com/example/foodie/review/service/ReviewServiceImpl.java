package com.example.foodie.review.service;

import com.example.foodie.dish.repository.DishRepository;
import com.example.foodie.order.entity.OrderDish;
import com.example.foodie.order.repository.OrderDishRepository;
import com.example.foodie.review.dto.response.ReviewResponseDTO;
import com.example.foodie.review.entity.Review;
import com.example.foodie.review.helper.ReviewHelper;
import com.example.foodie.review.mapper.ReviewMapper;
import com.example.foodie.review.repository.ReviewRepository;
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
