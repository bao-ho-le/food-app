package com.example.foodie.review.helper;

import com.example.foodie.order.entity.OrderDish;
import com.example.foodie.review.entity.Review;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class ReviewHelper {

    private static final int MIN_RATING = 1;
    private static final int MAX_RATING = 5;

    public void validateDishId(Integer dishId) {
        if (dishId == null || dishId <= 0) {
            throw new RuntimeException("Id món ăn không hợp lệ");
        }
    }

    public void validateOrderDishId(Integer orderDishId) {
        if (orderDishId == null || orderDishId <= 0) {
            throw new RuntimeException("Id món trong đơn hàng không hợp lệ");
        }
    }

    public void validateReviewRequest(Review review) {
        if (review == null) {
            throw new RuntimeException("Thông tin đánh giá không được để trống");
        }
        if (review.getRating() == null) {
            throw new RuntimeException("Điểm đánh giá không được để trống");
        }
        if (review.getRating() < MIN_RATING || review.getRating() > MAX_RATING) {
            throw new RuntimeException("Điểm đánh giá phải từ " + MIN_RATING + " đến " + MAX_RATING);
        }
    }

    public void validateNotReviewed(OrderDish orderDish) {
        if (orderDish.getReview() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Bạn đã review món này rồi");
        }
    }
}
