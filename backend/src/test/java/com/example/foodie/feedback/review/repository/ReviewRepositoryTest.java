package com.example.foodie.feedback.review.repository;

import com.example.foodie.catalog.dish.entity.Dish;
import com.example.foodie.catalog.restaurant.entity.Restaurant;
import com.example.foodie.feedback.review.entity.Review;
import com.example.foodie.identity.user.entity.Role;
import com.example.foodie.identity.user.entity.User;
import com.example.foodie.identity.user.enums.RoleName;
import com.example.foodie.ordering.order.entity.Order;
import com.example.foodie.ordering.order.enums.Status;
import com.example.foodie.support.AbstractMySqlDataJpaTest;
import com.example.foodie.support.TestDataFixtures;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * findAverageRatingByDishId dùng COALESCE(AVG(...), 0) để món chưa có đánh giá nào vẫn trả về
 * một số (0) thay vì null. Vì query luôn trả số, nhánh kiểm null ở tầng service là code chết
 * -- test item 1 ghi nhận sự thật đó; nếu ai bỏ COALESCE, test đỏ và biết ngay phải xem lại
 * cả tầng service.
 */
class ReviewRepositoryTest extends AbstractMySqlDataJpaTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ReviewRepository reviewRepository;

    private User user;
    private Dish dish;

    private void setUp() {
        Role role = TestDataFixtures.role(em, RoleName.USER);
        user = TestDataFixtures.user(em, role);
        Restaurant restaurant = TestDataFixtures.restaurant(em);
        dish = TestDataFixtures.dish(em, restaurant);
    }

    // ---- item 1: món chưa có đánh giá -> 0, không phải null ----

    @Test
    void should_returnZero_when_dishHasNoReview() {
        setUp();
        Float average = reviewRepository.findAverageRatingByDishId(dish.getId());
        assertThat(average).isEqualTo(0f);
    }

    // ---- item 2: trung bình đúng ----

    @Test
    void should_returnCorrectAverage_when_dishHasTwoReviews() {
        setUp();
        Order order = TestDataFixtures.order(em, user, Status.DELIVERED, 1f);

        // findAverageRatingByDishId truy vấn qua OrderDish.review (không phải Review đứng một
        // mình), nên phải gắn Review vào OrderDish rồi persist lại -- khác với
        // TestDataFixtures.review() dùng khi chỉ cần một Review độc lập.
        var orderDish1 = TestDataFixtures.orderDish(em, order, dish, 1, 10_000f);
        orderDish1.setReview(TestDataFixtures.review(em, 4));
        em.persistAndFlush(orderDish1);

        var orderDish2 = TestDataFixtures.orderDish(em, order, dish, 1, 10_000f);
        orderDish2.setReview(TestDataFixtures.review(em, 5));
        em.persistAndFlush(orderDish2);

        Float average = reviewRepository.findAverageRatingByDishId(dish.getId());
        assertThat(average).isEqualTo(4.5f);
    }
}
