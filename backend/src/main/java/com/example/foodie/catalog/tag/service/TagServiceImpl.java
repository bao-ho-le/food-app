package com.example.foodie.catalog.tag.service;

import com.example.foodie.catalog.tag.entity.Tag;
import com.example.foodie.catalog.tag.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {
    private final TagRepository tagRepository;

    @Override
    public List<Tag> getAllTags(){
        List<Tag> tags = tagRepository.findAll();

        if(tags.isEmpty()){
            throw new RuntimeException("Không tồn tại tag nào");
        }
        return tags;
    }
}
