package com.example.foodie.services.impl;

import com.example.foodie.services.interfaces.BaseService;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public abstract class BaseServiceImpl<T> implements BaseService<T> {
    protected final JpaRepository<T, Integer> repository;
    protected final Class<T> type;

    protected BaseServiceImpl(JpaRepository<T, Integer> repository, Class<T> type) {
        this.repository = repository;
        this.type = type;
    }

    @Override
    public List<T> getAll(){
        List<T> allObjects = repository.findAll();

        if(allObjects.isEmpty()){
            throw new RuntimeException("Không tồn tại " + type.getSimpleName() + " nào");
        }
        return allObjects;
    }

    @Override
    public T getById(Integer id){
        Optional<T> object = repository.findById(id);

        if(object.isEmpty()) {
            throw new RuntimeException("Không tồn tại " + type.getSimpleName() + " với id: " + id);
        }

        return object.get();
    }

    @Override
    public void deleteById(Integer id){
            repository.deleteById(id);
    }
}
