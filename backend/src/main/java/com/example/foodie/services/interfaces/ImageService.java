package com.example.foodie.services.interfaces;

import com.example.foodie.dtos.ImageDTO;
import com.example.foodie.models.Image;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ImageService {
    Map<String, Object> uploadImage(MultipartFile file, ImageDTO imageDTO);
    List<Image> getImagesByDishId(Integer dishId);
}
