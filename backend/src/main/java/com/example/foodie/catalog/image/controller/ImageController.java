package com.example.foodie.catalog.image.controller;

import com.example.foodie.catalog.image.entity.Image;
import com.example.foodie.catalog.image.service.ImageService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/images")
@AllArgsConstructor
public class ImageController {
    private ImageService imageService;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<Map<String, Object>> uploadImage(
            @RequestPart("file") MultipartFile file,
            @Valid @RequestPart("data") String json) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(imageService.uploadImage(file, json));
    }

    @GetMapping("/{dish_id}")
    public ResponseEntity<List<Image>> getImagesByDish(@PathVariable(name="dish_id") Integer dishId){
        return ResponseEntity.ok(imageService.getImagesByDishId(dishId));
    }
}
