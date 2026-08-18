package com.example.foodie.catalog.tag.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategorySummaryDTO {
    private Integer id;
    private String name;
}
