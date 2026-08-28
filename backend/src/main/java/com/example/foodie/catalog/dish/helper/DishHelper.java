package com.example.foodie.catalog.dish.helper;

import com.example.foodie.catalog.dish.dto.request.DishRequestDTO;
import com.example.foodie.common.exception.ErrorCode;
import com.example.foodie.common.exception.business_exception.CatalogException;
import org.springframework.stereotype.Component;

@Component
public class DishHelper {

    private static final int MAX_NAME_LENGTH = 255;

    public void validateDishId(Integer dishId) {
        if (dishId == null) {
            throw new CatalogException(ErrorCode.DISH_ID_REQUIRED);
        }
        if (dishId <= 0) {
            throw new CatalogException(ErrorCode.DISH_ID_INVALID);
        }
    }

    public void validateDishRequest(DishRequestDTO dishRequestDTO) {
        if (dishRequestDTO == null) {
            throw new CatalogException(ErrorCode.DISH_REQUEST_REQUIRED);
        }
        if (dishRequestDTO.getName() == null || dishRequestDTO.getName().isBlank()) {
            throw new CatalogException(ErrorCode.DISH_NAME_REQUIRED);
        }
        if (dishRequestDTO.getName().length() > MAX_NAME_LENGTH) {
            throw new CatalogException(ErrorCode.DISH_NAME_TOO_LONG);
        }
        if (dishRequestDTO.getPrice() < 0) {
            throw new CatalogException(ErrorCode.DISH_PRICE_NEGATIVE);
        }
        if (dishRequestDTO.getRestaurantId() == null || dishRequestDTO.getRestaurantId() <= 0) {
            throw new CatalogException(ErrorCode.RESTAURANT_ID_INVALID);
        }
        if (dishRequestDTO.getTags() == null) {
            throw new CatalogException(ErrorCode.DISH_TAGS_REQUIRED);
        }
    }

    public void validateBlockingType(Integer type) {
        if (type == null || (type != 0 && type != 1)) {
            throw new CatalogException(ErrorCode.DISH_BLOCK_TYPE_INVALID);
        }
    }

    public void validateStockTopUpQuantity(Integer quantity) {
        if (quantity == null) {
            throw new CatalogException(ErrorCode.DISH_STOCK_QUANTITY_REQUIRED);
        }
        if (quantity <= 0) {
            throw new CatalogException(ErrorCode.DISH_STOCK_QUANTITY_INVALID);
        }
    }
}
