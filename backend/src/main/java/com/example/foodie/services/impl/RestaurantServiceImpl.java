package com.example.foodie.services.impl;

import com.example.foodie.models.Restaurant;
import com.example.foodie.repos.RestaurantRepository;
import com.example.foodie.services.interfaces.RestaurantService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class RestaurantServiceImpl implements RestaurantService {
    private final RestaurantRepository restaurantRepository;

    @Override
    public List<Restaurant> getAllRestaurants(){

        List<Restaurant> restaurants = restaurantRepository.findAll();
        if (restaurants.isEmpty()){
            throw new RuntimeException("Không có nhà hàng nào");
        }else{
            return restaurants;
        }
    }

    @Override
    public Restaurant createRestaurant(Restaurant restaurant){

        if (restaurantRepository.existsByName(restaurant.getName())){
            throw new RuntimeException("Nhà hàng đã tồn tại");
        }
        if (restaurantRepository.existsByPhoneNumber(restaurant.getPhoneNumber())){
            throw new RuntimeException("Số điện thoại đã tồn tại");
        }
    
        Restaurant newRestaurant = Restaurant.builder()
                .name(restaurant.getName())
                .address(restaurant.getAddress())
                .phoneNumber(restaurant.getPhoneNumber())
                .build();

        return restaurantRepository.save(newRestaurant);
    }

    @Override
    public void blocking(Integer id, Integer type){
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhà hàng"));

        if(type == 0){
            restaurant.setAvailable(false);
        } else if(type == 1){
            restaurant.setAvailable(true);
        } else {
            throw new RuntimeException("Loại không hợp lệ");
        }
        restaurantRepository.save(restaurant);
    }

    @Override
    public Restaurant updateRestaurant(Integer id, Restaurant restaurant) {
        Restaurant existingRestaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhà hàng"));
        existingRestaurant.setName(restaurant.getName());
        existingRestaurant.setAddress(restaurant.getAddress());
        existingRestaurant.setPhoneNumber(restaurant.getPhoneNumber());
        return restaurantRepository.save(existingRestaurant);
    }
}
