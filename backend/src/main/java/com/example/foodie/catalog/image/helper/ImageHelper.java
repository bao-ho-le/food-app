package com.example.foodie.catalog.image.helper;

import com.example.foodie.catalog.image.dto.request.ImageDTO;
import com.example.foodie.common.exception.ErrorCode;
import com.example.foodie.common.exception.business_exception.CatalogException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ImageHelper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ImageDTO parseImageDTO(String json) {
        try {
            return objectMapper.readValue(json, ImageDTO.class);
        } catch (JsonProcessingException e) {
            throw new CatalogException(ErrorCode.IMAGE_DATA_INVALID, ErrorCode.IMAGE_DATA_INVALID.getMessage(), e);
        }
    }

    public void validateDishId(Integer dishId) {
        if (dishId == null) {
            throw new CatalogException(ErrorCode.DISH_ID_REQUIRED);
        }
        if (dishId <= 0) {
            throw new CatalogException(ErrorCode.DISH_ID_INVALID);
        }
    }

    public void validateUploadRequest(MultipartFile file, ImageDTO imageDTO) {
        if (imageDTO == null) {
            throw new CatalogException(ErrorCode.IMAGE_REQUEST_REQUIRED);
        }
        validateDishId(imageDTO.getDishId());

        if (file == null || file.isEmpty()) {
            throw new CatalogException(ErrorCode.IMAGE_FILE_EMPTY);
        }
    }
}
