package com.example.foodie.catalog.image.helper;

import com.example.foodie.catalog.image.dto.request.ImageDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ImageHelper {

    public void validateDishId(Integer dishId) {
        if (dishId == null || dishId <= 0) {
            throw new RuntimeException("Id món ăn không hợp lệ");
        }
    }

    public void validateUploadRequest(MultipartFile file, ImageDTO imageDTO) {
        if (imageDTO == null) {
            throw new RuntimeException("Thông tin ảnh không được để trống");
        }
        validateDishId(imageDTO.getDishId());

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Gửi file ảnh rỗng");
        }
    }
}
