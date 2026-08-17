package com.example.foodie.catalog.restaurant.service;

import com.example.foodie.catalog.restaurant.entity.Restaurant;
import com.example.foodie.catalog.restaurant.helper.RestaurantHelper;
import com.example.foodie.catalog.restaurant.repository.RestaurantRepository;
import com.example.foodie.common.exception.ErrorCode;
import com.example.foodie.common.exception.business_exception.CatalogException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RestaurantServiceImpl implements RestaurantService {
    private final RestaurantRepository restaurantRepository;
    private final RestaurantHelper restaurantHelper;

    @Override
    public List<Restaurant> getAllRestaurants(){
        List<Restaurant> restaurants = restaurantRepository.findAll();

        if (restaurants.isEmpty()){
            throw new CatalogException(ErrorCode.RESTAURANT_NOT_FOUND);
        }
        return restaurants;
    }

    @Override
    public Restaurant createRestaurant(Restaurant restaurant){
        restaurantHelper.validateRestaurantRequest(restaurant);

        if (restaurantRepository.existsByName(restaurant.getName())){
            throw new CatalogException(ErrorCode.RESTAURANT_NAME_ALREADY_EXISTS);
        }
        if (restaurantRepository.existsByPhoneNumber(restaurant.getPhoneNumber())){
            throw new CatalogException(ErrorCode.RESTAURANT_PHONE_ALREADY_EXISTS);
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
        restaurantHelper.validateRestaurantId(id);
        restaurantHelper.validateBlockingType(type);

        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new CatalogException(ErrorCode.RESTAURANT_NOT_FOUND));

        restaurant.setAvailable(type == 1);

        restaurantRepository.save(restaurant);
    }

    @Override
    public Restaurant updateRestaurant(Integer id, Restaurant restaurant) {
        restaurantHelper.validateRestaurantId(id);
        restaurantHelper.validateRestaurantRequest(restaurant);

        Restaurant existingRestaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new CatalogException(ErrorCode.RESTAURANT_NOT_FOUND));

        existingRestaurant.setName(restaurant.getName());
        existingRestaurant.setAddress(restaurant.getAddress());
        existingRestaurant.setPhoneNumber(restaurant.getPhoneNumber());

        return restaurantRepository.save(existingRestaurant);
    }
}
