package com.example.foodie.catalog.dishtag.service;

import com.example.foodie.catalog.dish.entity.Dish;
import com.example.foodie.catalog.dish.repository.DishRepository;
import com.example.foodie.catalog.dishtag.entity.DishTag;
import com.example.foodie.catalog.dishtag.helper.DishTagHelper;
import com.example.foodie.catalog.dishtag.repository.DishTagRepository;
import com.example.foodie.catalog.tag.entity.Tag;
import com.example.foodie.catalog.tag.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DishTagServiceImpl implements DishTagService {
    private final DishTagRepository dishTagRepository;
    private final DishRepository dishRepository;
    private final TagRepository tagRepository;
    private final DishTagHelper dishTagHelper;

    @Override
    public DishTag addTagForDish(int dishId, int tagId){
        dishTagHelper.validateDishId(dishId);
        dishTagHelper.validateTagId(tagId);

        Dish dish = dishRepository.findById(dishId).orElse(null);
        Tag tag = tagRepository.findById(tagId).orElse(null);

        if (dish == null || tag == null){
            throw new RuntimeException("Dish hoặc Tag không tồn tại");
        }
        if (dishTagRepository.existsByDish_IdAndTag_Id(dishId, tagId)){
            throw new RuntimeException("Dish đã có tag này rồi");
        }

        return dishTagRepository.save(DishTag.builder()
                .dish(dish)
                .tag(tag)
                .build());
    }
}
