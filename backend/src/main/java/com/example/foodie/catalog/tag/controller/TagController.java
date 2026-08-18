package com.example.foodie.catalog.tag.controller;

import com.example.foodie.catalog.category.entity.Category;
import com.example.foodie.catalog.tag.dto.response.CategorySummaryDTO;
import com.example.foodie.catalog.tag.dto.response.TagResponseDTO;
import com.example.foodie.catalog.tag.entity.Tag;
import com.example.foodie.catalog.tag.service.TagService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/tags")
@AllArgsConstructor
public class TagController {
    private final TagService tagService;

    @GetMapping
    public ResponseEntity<List<TagResponseDTO>> getAllTags(){
        List<TagResponseDTO> tags = tagService.getAllTags().stream()
                .map(TagController::toDto)
                .toList();
        return ResponseEntity.ok(tags);
    }

    private static TagResponseDTO toDto(Tag tag) {
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
