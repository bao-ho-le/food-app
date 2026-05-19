package com.example.foodie.controllers;

import com.example.foodie.services.interfaces.BaseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public abstract class BaseController<T> {

    protected final BaseService<T> baseService;

    public BaseController(BaseService<T> baseService){
        this.baseService = baseService;
    }

    @GetMapping
    public ResponseEntity<?> getAll(){
        try{
            List<T> allObjects = baseService.getAll();

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(allObjects);
        } catch(Exception e){
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id){
        try{
            T object = baseService.getById(id);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(object);
        } catch(Exception e){
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteById(@PathVariable Integer id){
        try{
            baseService.deleteById(id);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .build();
        } catch(Exception e){
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }
}
