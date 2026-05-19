package com.example.foodie.controllers;

import com.example.foodie.dtos.ImageDTO;
import com.example.foodie.models.Image;
import com.example.foodie.services.interfaces.ImageService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    public ResponseEntity<?> uploadImage(
            @RequestPart("file") MultipartFile file,
            @Valid @RequestPart("data") String json){

        try{
            ObjectMapper mapper = new ObjectMapper();
            ImageDTO imageDTO = mapper.readValue(json, ImageDTO.class);

            Map<String, Object> result = imageService.uploadImage(file, imageDTO);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(result);
        }catch (Exception e){
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @GetMapping("/{dish_id}")
    public ResponseEntity<?> getImagesByDish(@PathVariable(name="dish_id") Integer dishId){
        try{
            List<Image> images = imageService.getImagesByDishId(dishId);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(images);
        }catch (Exception e){
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }
}
