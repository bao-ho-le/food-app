package com.example.foodie.identity.admin.dashboard.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TopProductDTO {
    private Integer dishId;
    private String dishName;
    private String imageUrl;
    private double price;
    private long quantitySold;
}
