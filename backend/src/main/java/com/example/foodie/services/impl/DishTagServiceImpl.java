package com.example.foodie.services.impl;

import com.example.foodie.models.Dish;
import com.example.foodie.models.DishTag;
import com.example.foodie.models.Tag;
import com.example.foodie.repos.DishRepository;
import com.example.foodie.repos.DishTagRepository;
import com.example.foodie.repos.TagRepository;
import com.example.foodie.services.interfaces.DishTagService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class DishTagServiceImpl implements DishTagService {
    private DishTagRepository dishTagRepository;
    private DishRepository dishRepository;
    private TagRepository tagRepository;

    @Override
    public DishTag addTagForDish(int dishId, int tagId){
        Optional<Dish> dishOpt = dishRepository.findById(dishId);
        Optional<Tag> tagOpt = tagRepository.findById(tagId);

        boolean dishOrTagDontExist = !(dishOpt.isPresent() && tagOpt.isPresent());

        if (dishOrTagDontExist){
            throw new RuntimeException("Dish hoặc Tag không tồn tại");
        }
        else if (dishTagRepository.existsByDish_IdAndTag_Id(dishId, tagId)){
            throw new RuntimeException("Dish đã có tag này rồi");
        }
        else{
            DishTag newDishTag = DishTag.builder()
                    .dish(dishOpt.get())
                    .tag(tagOpt.get())
                    .build();

            return dishTagRepository.save(newDishTag);
        }
    }
}
