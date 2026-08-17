package com.example.foodie.image.service;

import com.example.foodie.image.dto.request.ImageDTO;
import com.example.foodie.image.entity.Image;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ImageService {
    Map<String, Object> uploadImage(MultipartFile file, ImageDTO imageDTO);
    List<Image> getImagesByDishId(Integer dishId);
}
