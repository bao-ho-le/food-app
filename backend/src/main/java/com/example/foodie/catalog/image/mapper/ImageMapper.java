package com.example.foodie.catalog.image.mapper;

import com.example.foodie.catalog.dish.entity.Dish;
import com.example.foodie.catalog.image.dto.request.ImageDTO;
import com.example.foodie.catalog.image.entity.Image;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ImageMapper {

    public Image toEntity(Map<String, Object> uploadResult, ImageDTO imageDTO, Dish dish) {
        return Image.builder()
                .url((String) uploadResult.get("secure_url"))
                .publicId((String) uploadResult.get("public_id"))
                .format((String) uploadResult.get("format"))
                .width((Integer) uploadResult.get("width"))
                .height((Integer) uploadResult.get("height"))
                .imageName(imageDTO.getImageName())
                .altText(imageDTO.getAltText())
                .isThumbnail(imageDTO.getIsThumbnail())
                .dish(dish)
                .build();
    }
}
