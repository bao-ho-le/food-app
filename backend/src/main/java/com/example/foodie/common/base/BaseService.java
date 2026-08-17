package com.example.foodie.common.base;

import java.util.List;

public interface BaseService<T> {
    List<T> getAll();
    T getById(Integer id);
    void deleteById(Integer id);
}
