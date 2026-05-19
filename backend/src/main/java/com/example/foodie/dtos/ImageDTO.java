package com.example.foodie.dtos;

import lombok.Data;

@Data
public class ImageDTO {
    private String imageName;
    private Boolean isThumbnail;
    private String altText;
    private Integer dishId;
}
