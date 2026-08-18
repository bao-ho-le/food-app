package com.example.foodie.catalog.tag.mapper;

import com.example.foodie.catalog.category.entity.Category;
import com.example.foodie.catalog.tag.dto.response.CategorySummaryDTO;
import com.example.foodie.catalog.tag.dto.response.TagResponseDTO;
import com.example.foodie.catalog.tag.entity.Tag;
import org.springframework.stereotype.Component;

@Component
public class TagMapper {

    public TagResponseDTO toDto(Tag tag) {
        Category category = tag.getCategory();
        return TagResponseDTO.builder()
                .id(tag.getId())
                .name(tag.getName())
                .category(category == null ? null : CategorySummaryDTO.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .build())
                .build();
    }
}
