package com.example.foodie.services.impl;

import com.example.foodie.dtos.ReviewResponseDTO;
import com.example.foodie.models.OrderDish;
import com.example.foodie.models.Review;
import com.example.foodie.repos.DishRepository;
import com.example.foodie.repos.OrderDishRepository;
import com.example.foodie.repos.ReviewRepository;
import com.example.foodie.services.interfaces.ReviewService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@AllArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private ReviewRepository reviewRepository;
    private DishRepository dishRepository;
    private OrderDishRepository orderDishRepository;

    @Override
    public List<ReviewResponseDTO> findAllReviewsByDishId(Integer dishId){
        dishRepository.findById(dishId)
                .orElseThrow(() -> new RuntimeException("Không tồn tại dish này"));

        
        List<OrderDish> orderDishes = orderDishRepository.findAllByDishId(dishId);

        // List<Review> reviews = orderDish.stream()
        //         .map(OrderDish::getReview)
        //         .filter(review -> review != null)
        //         .toList();

        // return reviews.stream()
        //         .map(review -> new ReviewResponseDTO(
        //                 orderDish.get(0).getOrder().getUser().getFullName(),
        //                 review.getComment(),
        //                 review.getRating(),
        //                 review.getCreatedAt()
        //         ))
        //         .toList();
        // return reviewRepository.findAllByDishId(dishId);
        return orderDishes.stream()
                .filter(od -> od.getReview() != null)
                .map(od -> new ReviewResponseDTO(
                        od.getOrder().getUser().getFullName(),
                        od.getReview().getComment(),
                        od.getReview().getRating(),
                        od.getReview().getCreatedAt()
                ))
                .toList();
    }

    @Override
    public Review addReview(Integer orderDishId, Review review){
        OrderDish orderDish = orderDishRepository.findById(orderDishId)
                .orElseThrow(() -> new RuntimeException("order dish không tồn tại"));

        if (orderDish.getReview() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Bạn đã review món này rồi");
        }

        Review newReview = Review.builder()
                .comment(review.getComment())
                .rating(review.getRating())
                .build();

        reviewRepository.save(newReview);
        orderDish.setReview(newReview);
        orderDishRepository.save(orderDish);

        return orderDish.getReview();
    }
}
