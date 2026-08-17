package com.example.foodie.restaurant.service;

import com.example.foodie.restaurant.entity.Restaurant;

import java.util.List;

public interface RestaurantService{
    public List<Restaurant> getAllRestaurants();
    public Restaurant createRestaurant(Restaurant restaurant);
    public void blocking(Integer id, Integer type);
    public Restaurant updateRestaurant(Integer id, Restaurant restaurant);
}
