package com.example.foodie.restaurant.service;

import com.example.foodie.restaurant.entity.Restaurant;
import com.example.foodie.restaurant.helper.RestaurantHelper;
import com.example.foodie.restaurant.repository.RestaurantRepository;
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
            throw new RuntimeException("Không có nhà hàng nào");
        }
        return restaurants;
    }

    @Override
    public Restaurant createRestaurant(Restaurant restaurant){
        restaurantHelper.validateRestaurantRequest(restaurant);

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
        restaurantHelper.validateRestaurantId(id);
        restaurantHelper.validateBlockingType(type);

        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhà hàng"));

        restaurant.setAvailable(type == 1);

        restaurantRepository.save(restaurant);
    }

    @Override
    public Restaurant updateRestaurant(Integer id, Restaurant restaurant) {
        restaurantHelper.validateRestaurantId(id);
        restaurantHelper.validateRestaurantRequest(restaurant);

        Restaurant existingRestaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhà hàng"));

        existingRestaurant.setName(restaurant.getName());
        existingRestaurant.setAddress(restaurant.getAddress());
        existingRestaurant.setPhoneNumber(restaurant.getPhoneNumber());

        return restaurantRepository.save(existingRestaurant);
    }
}
