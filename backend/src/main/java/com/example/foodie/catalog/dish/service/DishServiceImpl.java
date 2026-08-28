package com.example.foodie.catalog.dish.service;

import com.example.foodie.catalog.dish.dto.request.DishRequestDTO;
import com.example.foodie.catalog.dish.dto.request.DishStockRequestDTO;
import com.example.foodie.catalog.dish.dto.response.DishDTO;
import com.example.foodie.catalog.dish.entity.Dish;
import com.example.foodie.catalog.dish.helper.DishHelper;
import com.example.foodie.catalog.dish.mapper.DishMapper;
import com.example.foodie.catalog.dish.repository.DishRepository;
import com.example.foodie.catalog.image.entity.Image;
import com.example.foodie.catalog.restaurant.entity.Restaurant;
import com.example.foodie.feedback.review.entity.Review;
import com.example.foodie.catalog.image.repository.ImageRepository;
import com.example.foodie.feedback.review.repository.ReviewRepository;
import com.example.foodie.catalog.restaurant.entity.Restaurant;
import com.example.foodie.catalog.restaurant.repository.RestaurantRepository;
import com.example.foodie.catalog.dishtag.service.DishTagService;
import com.example.foodie.catalog.tag.dto.response.TagResponseDTO;
import com.example.foodie.catalog.tag.mapper.TagMapper;
import com.example.foodie.catalog.tag.repository.TagRepository;
import com.example.foodie.common.exception.ErrorCode;
import com.example.foodie.common.exception.business_exception.CatalogException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DishServiceImpl implements DishService {

    private final DishRepository dishRepository;
    private final TagRepository tagRepository;
    private final ReviewRepository reviewRepository;
    private final ImageRepository imageRepository;
    private final RestaurantRepository restaurantRepository;
    private final DishTagService dishTagService;
    private final DishHelper dishHelper;
    private final DishMapper dishMapper;
    private final TagMapper tagMapper;

    @Override
    public List<DishDTO> getAllDishes() {
        List<Dish> dishes = dishRepository.findAll();

        return dishes.stream()
                .map(dish -> dishMapper.toDto(
                        dish,
                        resolveAverageRating(dish.getId()),
                        getAllTags(dish.getId()),
                        resolveImageUrl(dish)))
                .toList();
    }

    @Override
    public List<TagResponseDTO> getAllTags(Integer dishId){
        dishHelper.validateDishId(dishId);

        return tagRepository.findTagsByDishId(dishId).stream()
                .map(tagMapper::toDto)
                .toList();
    }

    @Override
    public Dish createDish(DishRequestDTO dishRequestDTO){
        dishHelper.validateDishRequest(dishRequestDTO);

        Restaurant restaurant = restaurantRepository.findById(dishRequestDTO.getRestaurantId())
                .orElseThrow(() -> new CatalogException(ErrorCode.RESTAURANT_NOT_FOUND));

        Dish savedDish = dishRepository.save(dishMapper.toEntity(dishRequestDTO, restaurant));

        attachImage(savedDish, dishRequestDTO.getImageUrl());
        attachTags(savedDish, dishRequestDTO.getTags());

        return savedDish;
    }

    @Override
    public List<Float> getAverageRatings() {
        List<Dish> allDishes = dishRepository.findAll();
        List<Float> dishesRating = new ArrayList<>(Collections.nCopies(allDishes.size(), null));

        for (int i = 0; i < allDishes.size(); i++) {
            Dish dish = allDishes.get(i);
            float sum = 0;
            int count = 0;

            for (Review review : reviewRepository.findAllByDishId(dish.getId())) {
                if (review != null) {
                    sum += review.getRating();
                    count++;
                }
            }

            if (count > 0) {
                float avg = sum / count;
                dishesRating.set(i, avg);  // chỉ gán nếu có rating
            }
            // nếu count == 0, giữ nguyên null
        }

        return dishesRating;
    }

    @Override
    public List<Integer> getAllDishId(){
        return dishRepository.findAll().stream()
                .map(Dish::getId)
                .collect(Collectors.toList());
    }

    @Override
    public void blocking(Integer dishId, Integer type){
        dishHelper.validateDishId(dishId);
        dishHelper.validateBlockingType(type);

        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(() -> new CatalogException(ErrorCode.DISH_NOT_FOUND));

        dish.setAvailable(type == 1);

        dishRepository.save(dish);
    }

    @Transactional
    @Override
    public Dish restockDish(Integer dishId, DishStockRequestDTO dishStockRequestDTO) {
        dishHelper.validateDishId(dishId);
        dishHelper.validateStockTopUpQuantity(dishStockRequestDTO.getQuantity());

        Dish dish = dishRepository.findByIdForUpdate(dishId)
                .orElseThrow(() -> new CatalogException(ErrorCode.DISH_NOT_FOUND));

        dish.setStockQuantity(dish.getStockQuantity() + dishStockRequestDTO.getQuantity());

        Dish saved = dishRepository.save(dish);
        saved.setRestaurant((Restaurant) Hibernate.unproxy(saved.getRestaurant()));
        return saved;
    }

    // Helper

    private float resolveAverageRating(Integer dishId) {
        Float avgRating = reviewRepository.findAverageRatingByDishId(dishId);

        return avgRating != null ? avgRating : 0f; // nếu null thì mặc định 0
    }

    private String resolveImageUrl(Dish dish) {
        Image image = imageRepository.findByDish_Id(dish.getId()).stream()
                .findFirst()
                .orElse(null);

        if (image == null) {
            System.out.println("⚠️ Không có ảnh cho món: " + dish.getName());
            return "";
        }
        return image.getUrl();
    }

    private void attachImage(Dish dish, String imageUrl) {
        imageRepository.save(Image.builder()
                .url(imageUrl)
                .dish(dish)
                .build());
    }

    private void attachTags(Dish dish, List<Integer> tagIds) {
        for (Integer tagId : tagIds) {
            dishTagService.addTagForDish(dish.getId(), tagId);
        }
    }
}
