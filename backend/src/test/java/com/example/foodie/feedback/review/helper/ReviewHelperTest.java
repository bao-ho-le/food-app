package com.example.foodie.feedback.review.helper;

import com.example.foodie.common.exception.ErrorCode;
import com.example.foodie.common.exception.business_exception.FeedbackException;
import com.example.foodie.identity.user.entity.User;
import com.example.foodie.ordering.order.entity.Order;
import com.example.foodie.ordering.order.entity.OrderDish;
import com.example.foodie.ordering.order.enums.Status;
import com.example.foodie.feedback.review.entity.Review;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Ba điều kiện độc lập để được đánh giá một món trong đơn. Item "khác chủ đơn" là
// kiểm tra chống horizontal privilege escalation — nếu mất, bất kỳ ai cũng đánh
// giá được đơn của người khác.
class ReviewHelperTest {

    private final ReviewHelper reviewHelper = new ReviewHelper();

    private static OrderDish orderDishWithStatus(Status status) {
        Order order = Order.builder().status(status).build();
        return OrderDish.builder().order(order).build();
    }

    private static OrderDish orderDishOwnedBy(Integer ownerId) {
        User owner = User.builder().id(ownerId).build();
        Order order = Order.builder().user(owner).build();
        return OrderDish.builder().order(order).build();
    }

    @Test
    @DisplayName("validateOrderDelivered không ném khi đơn DELIVERED")
    void should_notThrow_when_orderIsDelivered() {
        assertThatCode(() -> reviewHelper.validateOrderDelivered(orderDishWithStatus(Status.DELIVERED)))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @EnumSource(value = Status.class, names = "DELIVERED", mode = EnumSource.Mode.EXCLUDE)
    @DisplayName("validateOrderDelivered ném ORDER_NOT_DELIVERED khi đơn chưa DELIVERED")
    void should_throwOrderNotDelivered_when_orderIsNotDelivered(Status status) {
        assertThatThrownBy(() -> reviewHelper.validateOrderDelivered(orderDishWithStatus(status)))
                .isInstanceOf(FeedbackException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ORDER_NOT_DELIVERED);
    }

    @Test
    @DisplayName("validateOrderOwner không ném khi orderDish.order.user.id trùng id người gọi")
    void should_notThrow_when_callerIsOrderOwner() {
        OrderDish orderDish = orderDishOwnedBy(1);
        User caller = User.builder().id(1).build();

        assertThatCode(() -> reviewHelper.validateOrderOwner(orderDish, caller)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateOrderOwner ném REVIEW_NOT_ORDER_OWNER khi id khác nhau")
    void should_throwReviewNotOrderOwner_when_callerIsNotOrderOwner() {
        OrderDish orderDish = orderDishOwnedBy(1);
        User caller = User.builder().id(2).build();

        assertThatThrownBy(() -> reviewHelper.validateOrderOwner(orderDish, caller))
                .isInstanceOf(FeedbackException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.REVIEW_NOT_ORDER_OWNER);
    }

    @Test
    @DisplayName("validateNotReviewed không ném khi orderDish.review == null")
    void should_notThrow_when_orderDishHasNoReviewYet() {
        OrderDish orderDish = OrderDish.builder().review(null).build();

        assertThatCode(() -> reviewHelper.validateNotReviewed(orderDish)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateNotReviewed ném REVIEW_ALREADY_EXISTS khi orderDish.review != null")
    void should_throwReviewAlreadyExists_when_orderDishAlreadyHasReview() {
        OrderDish orderDish = OrderDish.builder().review(Review.builder().id(1).build()).build();

        assertThatThrownBy(() -> reviewHelper.validateNotReviewed(orderDish))
                .isInstanceOf(FeedbackException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.REVIEW_ALREADY_EXISTS);
    }
}
