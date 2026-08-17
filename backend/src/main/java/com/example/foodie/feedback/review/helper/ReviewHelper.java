package com.example.foodie.feedback.review.helper;

import com.example.foodie.common.exception.ErrorCode;
import com.example.foodie.common.exception.business_exception.CatalogException;
import com.example.foodie.common.exception.business_exception.FeedbackException;
import com.example.foodie.ordering.order.entity.OrderDish;
import com.example.foodie.feedback.review.entity.Review;
import org.springframework.stereotype.Component;

@Component
public class ReviewHelper {

    private static final int MIN_RATING = 1;
    private static final int MAX_RATING = 5;

    public void validateDishId(Integer dishId) {
        if (dishId == null) {
            throw new CatalogException(ErrorCode.DISH_ID_REQUIRED);
        }
        if (dishId <= 0) {
            throw new CatalogException(ErrorCode.DISH_ID_INVALID);
        }
    }

    public void validateOrderDishId(Integer orderDishId) {
        if (orderDishId == null) {
            throw new FeedbackException(ErrorCode.REVIEW_ORDER_DISH_ID_REQUIRED);
        }
        if (orderDishId <= 0) {
            throw new FeedbackException(ErrorCode.REVIEW_ORDER_DISH_ID_INVALID);
        }
    }

    public void validateReviewRequest(Review review) {
        if (review == null) {
            throw new FeedbackException(ErrorCode.REVIEW_REQUEST_REQUIRED);
        }
        if (review.getRating() == null) {
            throw new FeedbackException(ErrorCode.REVIEW_RATING_REQUIRED);
        }
        if (review.getRating() < MIN_RATING || review.getRating() > MAX_RATING) {
            throw new FeedbackException(ErrorCode.REVIEW_RATING_OUT_OF_RANGE);
        }
    }

    public void validateNotReviewed(OrderDish orderDish) {
        if (orderDish.getReview() != null) {
            throw new FeedbackException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }
    }
}
