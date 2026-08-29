package com.example.foodie.catalog.restaurant.repository;

import com.example.foodie.catalog.restaurant.entity.Restaurant;
import com.example.foodie.support.AbstractMySqlDataJpaTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * countActiveRestaurants lọc theo isAvailable = true bằng JPQL viết tay (không phải derived
 * query) -- rủi ro chính là đảo ngược điều kiện hoặc đếm nhầm cả nhà hàng đã ẩn.
 */
class RestaurantRepositoryTest extends AbstractMySqlDataJpaTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Test
    void should_countOnlyAvailableRestaurants_when_mixedAvailabilityExists() {
        em.persistAndFlush(Restaurant.builder().name("Active 1").isAvailable(true).build());
        em.persistAndFlush(Restaurant.builder().name("Active 2").isAvailable(true).build());
        em.persistAndFlush(Restaurant.builder().name("Active 3").isAvailable(true).build());
        em.persistAndFlush(Restaurant.builder().name("Hidden 1").isAvailable(false).build());
        em.persistAndFlush(Restaurant.builder().name("Hidden 2").isAvailable(false).build());

        assertThat(restaurantRepository.countActiveRestaurants()).isEqualTo(3);
    }
}
