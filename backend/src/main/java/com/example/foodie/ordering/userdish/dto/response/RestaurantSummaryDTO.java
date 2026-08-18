package com.example.foodie.ordering.userdish.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RestaurantSummaryDTO {
    private Integer id;
    private String name;
}
