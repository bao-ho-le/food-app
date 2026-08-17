package com.example.foodie.common.base;

import com.example.foodie.common.exception.ErrorCode;
import com.example.foodie.common.exception.business_exception.BusinessException;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public abstract class BaseServiceImpl<T> implements BaseService<T> {
    protected final JpaRepository<T, Integer> repository;
    protected final Class<T> type;
    protected final ErrorCode notFoundErrorCode;

    protected BaseServiceImpl(JpaRepository<T, Integer> repository, Class<T> type, ErrorCode notFoundErrorCode) {
        this.repository = repository;
        this.type = type;
        this.notFoundErrorCode = notFoundErrorCode;
    }

    @Override
    public List<T> getAll(){
        List<T> allObjects = repository.findAll();

        if(allObjects.isEmpty()){
            throw new BusinessException(notFoundErrorCode);
        }
        return allObjects;
    }

    @Override
    public T getById(Integer id){
        Optional<T> object = repository.findById(id);

        if(object.isEmpty()) {
            throw new BusinessException(notFoundErrorCode);
        }

        return object.get();
    }

    @Override
    public void deleteById(Integer id){
            repository.deleteById(id);
    }
}
