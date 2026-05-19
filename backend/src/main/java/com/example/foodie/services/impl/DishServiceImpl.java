package com.example.foodie.services.impl;

import com.example.foodie.dtos.DishDTO;
import com.example.foodie.dtos.DishRequestDTO;
import com.example.foodie.models.*;
import com.example.foodie.repos.DishRepository;
import com.example.foodie.repos.ImageRepository;
import com.example.foodie.repos.ReviewRepository;
import com.example.foodie.repos.TagRepository;
import com.example.foodie.services.interfaces.DishService;
import com.example.foodie.services.interfaces.ImageService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.foodie.repos.RestaurantRepository;
import com.example.foodie.services.interfaces.DishTagService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class DishServiceImpl implements DishService {

    private DishRepository dishRepository;
    private TagRepository tagRepository;
    private ReviewRepository reviewRepository;
    private ImageRepository imageRepository;
    private ImageService imageService;
    private RestaurantRepository restaurantRepository;
    private DishTagService dishTagService;


    @Override
    public List<DishDTO> getAllDishes() {
        List<Dish> dishes = dishRepository.findAll();

        if (dishes.isEmpty()) {
            throw new RuntimeException("Không có món nào");
        }

        return dishes.stream()
                .map(dish -> {
                    // Lấy tag của món theo id
                    List<Tag> listTag = getAllTags(dish.getId());

                    // Lấy tất cả review của món và tính rating trung bình
                    Float avgRatingObj = reviewRepository.findAverageRatingByDishId(dish.getId());
                    float avgRating = avgRatingObj != null ? avgRatingObj : 0f; // nếu null thì mặc định 0

                    Image image = imageRepository.findByDish_Id(dish.getId()).stream()
                            .findFirst()
                            .orElse(null);

                    String url = "";
                    if (image == null) {
                        System.out.println("⚠️ Không có ảnh cho món: " + dish.getName());
                    } else{
                        url = image.getUrl();
                    }


                    return DishDTO.builder()
                            .id(dish.getId())
                            .name(dish.getName())
                            .price(dish.getPrice())
                            .isAvailable(dish.isAvailable()) // builder method đúng kiểu Boolean
                            .restaurant(dish.getRestaurant())
                            .rating(avgRating)
                            .tags(listTag)
                            .url(url)
                            .build();
                })
                .toList();

    }



    @Override
    public List<Tag> getAllTags(Integer dishId){

        List<Tag> listTag = tagRepository.findTagsByDishId(dishId);

        return listTag;
    }

    @Override
    public Dish createDish(DishRequestDTO dishRequestDTO){
        Restaurant restaurant = restaurantRepository.findById(dishRequestDTO.getRestaurantId()).orElseThrow(() -> new RuntimeException("Nha Hang không tồn tại"));

        Dish newDish = Dish.builder()
                .name(dishRequestDTO.getName())
                .price(dishRequestDTO.getPrice())
                .restaurant(restaurant)
                .isAvailable(true)
                .build();
        Dish savedDish = dishRepository.save(newDish);
        Image newImage = Image.builder()
                .url(dishRequestDTO.getImageUrl())
                .dish(savedDish)
                .build();

        imageRepository.save(newImage);

        for (Integer tagId : dishRequestDTO.getTags()) {
            dishTagService.addTagForDish(savedDish.getId(), tagId);
        }


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
        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(() -> new RuntimeException("Dish không tồn tại"));

        if (type == 0){
            dish.setAvailable(false);
        }
        else if (type == 1){
            dish.setAvailable(true);
        }
        else{
            throw new RuntimeException("Type không hợp lệ");
        }

        dishRepository.save(dish);
    }

}
