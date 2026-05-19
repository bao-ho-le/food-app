package com.example.foodie.services.impl;

import com.example.foodie.dtos.UserDishDTO;
import com.example.foodie.models.Tag;
import com.example.foodie.models.UserDish;
import com.example.foodie.repos.TagRepository;
import com.example.foodie.services.interfaces.TagService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class TagServiceImpl implements TagService {
    private TagRepository tagRepository;

    @Override
    public List<Tag> getAllTags(){
        List<Tag> tags = tagRepository.findAll();

        if(tags.isEmpty()){
            throw new RuntimeException("Không tồn tại tag nào");
        }else{
            return tags;
        }
    }
}
