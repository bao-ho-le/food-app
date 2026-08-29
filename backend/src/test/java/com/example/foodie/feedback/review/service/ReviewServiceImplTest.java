package com.example.foodie.feedback.review.service;

import com.example.foodie.catalog.dish.repository.DishRepository;
import com.example.foodie.common.exception.ErrorCode;
import com.example.foodie.common.exception.business_exception.FeedbackException;
import com.example.foodie.common.exception.business_exception.OrderingException;
import com.example.foodie.feedback.review.entity.Review;
import com.example.foodie.feedback.review.helper.ReviewHelper;
import com.example.foodie.feedback.review.mapper.ReviewMapper;
import com.example.foodie.feedback.review.repository.ReviewRepository;
import com.example.foodie.identity.user.entity.User;
import com.example.foodie.identity.user.helper.UserHelper;
import com.example.foodie.identity.user.repository.UserRepository;
import com.example.foodie.ordering.order.entity.Order;
import com.example.foodie.ordering.order.entity.OrderDish;
import com.example.foodie.ordering.order.enums.Status;
import com.example.foodie.ordering.order.repository.OrderDishRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

// Ba điều kiện đồng thời để đánh giá được: sở hữu đơn ∧ đơn đã DELIVERED ∧ chưa đánh giá.
// DishRepository ở đây không tham gia vào addReview (chỉ dùng ở findAllReviewsByDishId),
// nên mock nhưng không cần stub cho các test addReview.
@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private DishRepository dishRepository;
    @Mock
    private OrderDishRepository orderDishRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private Authentication authentication;

    private ReviewServiceImpl reviewService;
    private User caller;

    @BeforeEach
    void setUp() {
        caller = User.builder().id(1).email("caller@test.local").build();
        UserHelper userHelper = new UserHelper(userRepository);
        reviewService = new ReviewServiceImpl(reviewRepository, dishRepository, orderDishRepository,
                new ReviewHelper(), new ReviewMapper(), userHelper);
    }

    private void stubCaller() {
        when(authentication.getName()).thenReturn("caller@test.local");
        when(userRepository.findByEmail("caller@test.local")).thenReturn(Optional.of(caller));
    }

    private static Review reviewRequest(int rating, String comment) {
        return Review.builder().rating(rating).comment(comment).build();
    }

    @Test
    @DisplayName("orderDishId không tồn tại ném ORDER_DISH_NOT_FOUND")
    void should_throwOrderDishNotFound_when_orderDishIdDoesNotExist() {
        stubCaller();
        when(orderDishRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.addReview(authentication, 999, reviewRequest(4, "ngon")))
                .isInstanceOf(OrderingException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ORDER_DISH_NOT_FOUND);
    }

    @Test
    @DisplayName("Đơn thuộc user khác ném REVIEW_NOT_ORDER_OWNER")
    void should_throwReviewNotOrderOwner_when_orderBelongsToAnotherUser() {
        stubCaller();
        User otherUser = User.builder().id(2).build();
        Order order = Order.builder().id(1).user(otherUser).status(Status.DELIVERED).build();
        OrderDish orderDish = OrderDish.builder().id(1).order(order).build();
        when(orderDishRepository.findById(1)).thenReturn(Optional.of(orderDish));

        assertThatThrownBy(() -> reviewService.addReview(authentication, 1, reviewRequest(4, "ngon")))
                .isInstanceOf(FeedbackException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.REVIEW_NOT_ORDER_OWNER);
    }

    @Test
    @DisplayName("Đơn của chính mình nhưng chưa DELIVERED ném ORDER_NOT_DELIVERED")
    void should_throwOrderNotDelivered_when_ownOrderIsNotYetDelivered() {
        stubCaller();
        Order order = Order.builder().id(1).user(caller).status(Status.DELIVERING).build();
        OrderDish orderDish = OrderDish.builder().id(1).order(order).build();
        when(orderDishRepository.findById(1)).thenReturn(Optional.of(orderDish));

        assertThatThrownBy(() -> reviewService.addReview(authentication, 1, reviewRequest(4, "ngon")))
                .isInstanceOf(FeedbackException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ORDER_NOT_DELIVERED);
    }

    @Test
    @DisplayName("Đơn của mình, DELIVERED, đã có đánh giá ném REVIEW_ALREADY_EXISTS")
    void should_throwReviewAlreadyExists_when_orderDishAlreadyReviewed() {
        stubCaller();
        Order order = Order.builder().id(1).user(caller).status(Status.DELIVERED).build();
        Review previousReview = Review.builder().id(5).rating(3).build();
        OrderDish orderDish = OrderDish.builder().id(1).order(order).review(previousReview).build();
        when(orderDishRepository.findById(1)).thenReturn(Optional.of(orderDish));

        assertThatThrownBy(() -> reviewService.addReview(authentication, 1, reviewRequest(4, "ngon")))
                .isInstanceOf(FeedbackException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.REVIEW_ALREADY_EXISTS);
    }

    // Kiểm nội dung lưu thực sự đúng (Quy tắc 2), đồng thời khẳng định: id client gửi kèm
    // trong body request (nếu có) không làm ghi đè review của người khác, vì ReviewMapper
    // dựng entity mới chỉ từ rating/comment, không mang id từ input.
    @Test
    @DisplayName("Đơn của mình, DELIVERED, chưa đánh giá -> review được lưu đúng rating/comment và gắn vào OrderDish")
    void should_saveReviewAndAttachToOrderDish_when_allConditionsMet() {
        stubCaller();
        Order order = Order.builder().id(1).user(caller).status(Status.DELIVERED).build();
        OrderDish orderDish = OrderDish.builder().id(1).order(order).build();
        when(orderDishRepository.findById(1)).thenReturn(Optional.of(orderDish));
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderDishRepository.save(any(OrderDish.class))).thenAnswer(inv -> inv.getArgument(0));

        Review clientSuppliedReview = Review.builder().id(999).rating(4).comment("Rat ngon").build();
        Review result = reviewService.addReview(authentication, 1, clientSuppliedReview);

        ArgumentCaptor<Review> captor = ArgumentCaptor.forClass(Review.class);
        org.mockito.Mockito.verify(reviewRepository).save(captor.capture());
        assertThat(captor.getValue().getRating()).isEqualTo(4);
        assertThat(captor.getValue().getComment()).isEqualTo("Rat ngon");
        assertThat(captor.getValue().getId()).isNull();
        assertThat(result).isSameAs(orderDish.getReview());
    }
}
