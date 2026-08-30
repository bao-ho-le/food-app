package com.example.foodie.catalog.dishtag.service;

import com.example.foodie.catalog.dish.entity.Dish;
import com.example.foodie.catalog.dish.repository.DishRepository;
import com.example.foodie.catalog.dishtag.entity.DishTag;
import com.example.foodie.catalog.dishtag.helper.DishTagHelper;
import com.example.foodie.catalog.dishtag.repository.DishTagRepository;
import com.example.foodie.catalog.tag.entity.Tag;
import com.example.foodie.catalog.tag.repository.TagRepository;
import com.example.foodie.common.exception.ErrorCode;
import com.example.foodie.common.exception.business_exception.CatalogException;
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

        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(() -> new CatalogException(ErrorCode.DISH_NOT_FOUND));
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new CatalogException(ErrorCode.TAG_NOT_FOUND));

        if (dishTagRepository.existsByDish_IdAndTag_Id(dishId, tagId)){
            throw new CatalogException(ErrorCode.DISHTAG_ALREADY_EXISTS);
        }

        return dishTagRepository.save(DishTag.builder()
                .dish(dish)
                .tag(tag)
                .build());
    }

    @Override
    public void removeAllTagsForDish(int dishId) {
        dishTagHelper.validateDishId(dishId);
        dishTagRepository.deleteByDish_Id(dishId);
    }
}
