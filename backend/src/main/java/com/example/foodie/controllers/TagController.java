package com.example.foodie.controllers;

import com.example.foodie.models.Tag;
import com.example.foodie.services.interfaces.TagService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/tags")
@AllArgsConstructor
public class TagController {
    private TagService tagService;

    @GetMapping
    public ResponseEntity<?> getAllTags(){
        try{
            List<Tag> tags = tagService.getAllTags();

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(tags);
        } catch(Exception e){
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }
}
