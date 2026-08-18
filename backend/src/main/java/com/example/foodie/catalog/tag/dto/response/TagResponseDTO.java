package com.example.foodie.catalog.tag.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TagResponseDTO {
    private Integer id;
    private String name;
    private CategorySummaryDTO category;
}
