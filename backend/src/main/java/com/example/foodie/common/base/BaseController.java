package com.example.foodie.common.base;

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
    public ResponseEntity<List<T>> getAll(){
        return ResponseEntity.ok(baseService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<T> getById(@PathVariable Integer id){
        return ResponseEntity.ok(baseService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Integer id){
        baseService.deleteById(id);

        return ResponseEntity.ok().build();
    }
}
