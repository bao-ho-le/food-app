package com.example.foodie.catalog.tag.repository;

import com.example.foodie.catalog.category.entity.Category;
import com.example.foodie.catalog.dish.entity.Dish;
import com.example.foodie.catalog.restaurant.entity.Restaurant;
import com.example.foodie.catalog.tag.entity.Tag;
import com.example.foodie.support.AbstractMySqlDataJpaTest;
import com.example.foodie.support.TestDataFixtures;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * findTagsByDishId đi qua bảng nối dish_tag (JOIN) -- rủi ro chính là join sai hướng, làm lộ
 * tag của món khác hoặc nhân bản dòng.
 */
class TagRepositoryTest extends AbstractMySqlDataJpaTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private TagRepository tagRepository;

    // ---- item 1: chỉ trả tag của đúng dish, không lẫn, không trùng ----

    @Test
    void should_returnOnlyTagsOfGivenDish_when_multipleDishesHaveTags() {
        Restaurant restaurant = TestDataFixtures.restaurant(em);
        Dish dishA = TestDataFixtures.dish(em, restaurant);
        Dish dishB = TestDataFixtures.dish(em, restaurant);
        Category category = TestDataFixtures.category(em);

        Tag tag1 = TestDataFixtures.tag(em, category);
        Tag tag2 = TestDataFixtures.tag(em, category);
        Tag tagOfB = TestDataFixtures.tag(em, category);

        TestDataFixtures.dishTag(em, dishA, tag1);
        TestDataFixtures.dishTag(em, dishA, tag2);
        TestDataFixtures.dishTag(em, dishB, tagOfB);

        List<Tag> tagsOfA = tagRepository.findTagsByDishId(dishA.getId());

        assertThat(tagsOfA).hasSize(2);
        assertThat(tagsOfA).extracting(Tag::getId).containsExactlyInAnyOrder(tag1.getId(), tag2.getId());
    }
}
