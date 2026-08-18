package com.example.foodie.ordering.userdish.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DishSummaryDTO {
    private Integer id;
    private String name;
    private float price;
    private String url;
    private boolean available;
    private RestaurantSummaryDTO restaurant;
}
