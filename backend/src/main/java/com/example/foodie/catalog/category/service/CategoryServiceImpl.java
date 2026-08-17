package com.example.foodie.catalog.category.service;

import com.example.foodie.catalog.category.entity.Category;
import com.example.foodie.catalog.category.repository.CategoryRepository;
import com.example.foodie.common.exception.ErrorCode;
import com.example.foodie.common.exception.business_exception.CatalogException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    @Override
    public List<Category> getAllCategories(){
        List<Category> categories = categoryRepository.findAll();

        if(categories.isEmpty()){
            throw new CatalogException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        return categories;
    }
}
