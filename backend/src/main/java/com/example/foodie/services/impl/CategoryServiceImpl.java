package com.example.foodie.services.impl;

import com.example.foodie.models.Category;
import com.example.foodie.repos.CategoryRepository;
import com.example.foodie.services.interfaces.CategoryService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private CategoryRepository categoryRepository;

    @Override
    public List<Category> getAllCategories(){
        List<Category> categories = categoryRepository.findAll();

        if(categories.isEmpty()){
            throw new RuntimeException("Không tồn tại category nào");
        }else{
            return categories;
        }
    }
}
