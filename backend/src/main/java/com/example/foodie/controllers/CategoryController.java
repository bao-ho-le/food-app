package com.example.foodie.controllers;

import com.example.foodie.models.Category;
import com.example.foodie.services.interfaces.CategoryService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/categories")
@AllArgsConstructor
public class CategoryController {
    private CategoryService categoryService;

    @GetMapping
    public ResponseEntity<?> getAllCategories(){
        try{
            List<Category> categories = categoryService.getAllCategories();

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(categories);
        } catch(Exception e){
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }
}
