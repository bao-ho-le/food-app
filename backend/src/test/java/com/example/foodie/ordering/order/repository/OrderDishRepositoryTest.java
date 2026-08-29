package com.example.foodie.ordering.order.repository;

import com.example.foodie.catalog.dish.entity.Dish;
import com.example.foodie.catalog.restaurant.entity.Restaurant;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * sumQuantityByDishForOrderStatus xếp hạng món bán chạy cho dashboard admin -- phải cộng
 * đúng theo dish, chỉ tính đơn ở status truyền vào, và sắp xếp giảm dần theo tổng số lượng.
 */
class OrderDishRepositoryTest extends AbstractMySqlDataJpaTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private OrderDishRepository orderDishRepository;

    private User user;
    private Restaurant restaurant;

    private void setUp() {
        Role role = TestDataFixtures.role(em, RoleName.USER);
        user = TestDataFixtures.user(em, role);
        restaurant = TestDataFixtures.restaurant(em);
    }

    // ---- item 1: cộng đúng theo dish, sắp xếp giảm dần ----

    @Test
    void should_sumQuantityPerDishInDescendingOrder_when_multipleDeliveredOrdersExist() {
        setUp();
        Dish dishA = TestDataFixtures.dish(em, restaurant);
        Dish dishB = TestDataFixtures.dish(em, restaurant);

        Order order1 = TestDataFixtures.order(em, user, Status.DELIVERED, 1f);
        TestDataFixtures.orderDish(em, order1, dishA, 3, 10_000f);
        Order order2 = TestDataFixtures.order(em, user, Status.DELIVERED, 1f);
        TestDataFixtures.orderDish(em, order2, dishA, 4, 10_000f);
        Order order3 = TestDataFixtures.order(em, user, Status.DELIVERED, 1f);
        TestDataFixtures.orderDish(em, order3, dishB, 5, 8_000f);

        List<Object[]> result = orderDishRepository.sumQuantityByDishForOrderStatus(Status.DELIVERED);

        assertThat(result).hasSize(2);
        assertThat(result.get(0)[0]).isEqualTo(dishA.getId());
        assertThat(((Number) result.get(0)[1]).longValue()).isEqualTo(7);
        assertThat(result.get(1)[0]).isEqualTo(dishB.getId());
        assertThat(((Number) result.get(1)[1]).longValue()).isEqualTo(5);
    }

    // ---- item 2: chỉ tính đúng status, đơn PENDING không được cộng vào ----

    @Test
    void should_ignoreOrdersWithOtherStatus_when_summingQuantity() {
        setUp();
        Dish dishA = TestDataFixtures.dish(em, restaurant);
        Dish dishB = TestDataFixtures.dish(em, restaurant);

        Order delivered = TestDataFixtures.order(em, user, Status.DELIVERED, 1f);
        TestDataFixtures.orderDish(em, delivered, dishA, 3, 10_000f);
        Order pending = TestDataFixtures.order(em, user, Status.PENDING, 1f);
        TestDataFixtures.orderDish(em, pending, dishB, 100, 8_000f);

        List<Object[]> result = orderDishRepository.sumQuantityByDishForOrderStatus(Status.DELIVERED);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)[0]).isEqualTo(dishA.getId());
        assertThat(((Number) result.get(0)[1]).longValue()).isEqualTo(3);
    }
}
