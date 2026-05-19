package com.example.foodie.services.impl;

import com.example.foodie.dtos.OrderDishResponseDTO;
import com.example.foodie.enums.Status;
import com.example.foodie.models.*;
import com.example.foodie.repos.*;
import com.example.foodie.services.interfaces.OrderService;
import jakarta.transaction.Transactional;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrderServiceImpl extends BaseServiceImpl<Order> implements OrderService {
    private final OrderRepository orderRepository;
    private final UserDishRepository userDishRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final OrderDishRepository orderDishRepository;
    private final ImageRepository imageRepository;

    public OrderServiceImpl(OrderRepository orderRepository,
                            UserDishRepository userDishRepository,
                            UserRepository userRepository,
                            AddressRepository addressRepository,
                            OrderDishRepository orderDishRepository,
                            ImageRepository imageRepository) {
        super(orderRepository, Order.class);
        this.orderRepository = orderRepository;
        this.userDishRepository = userDishRepository;
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.orderDishRepository = orderDishRepository;
        this.imageRepository = imageRepository;
    }

    @Override
    public List<Order> getAllOrdersByUserId(Authentication authentication){
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        List<Order> allOrders = orderRepository.findAllByUser_Id(user.getId());
        return allOrders;
    }

    @Transactional
    @Override
    public Order createOrder(Authentication authentication, Integer addressId){
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        List<UserDish> allUserDishByUserId = userDishRepository.findAllByUser_Id(user.getId());

        if(allUserDishByUserId.isEmpty()){
            throw new RuntimeException("Không có user dish nào");
        }

        Optional<Address> address = addressRepository.findById(addressId);

        if (address.isEmpty()){
            throw new RuntimeException("Không tồn tại địa chỉ này");
        }

        Order tempOrder = Order.builder()
                .user(user)
                .deliveryAddress(address.get().getAddress())
                .totalPrice(0f)
                .status(Status.DELIVERED)
                .build();
        
        Order newOrder = orderRepository.save(tempOrder);
        

        List<OrderDish> orderDishes = new ArrayList<>();

        for (UserDish userDish: allUserDishByUserId){
            if (userDish.getQuantity() <= 0 || userDish.getDish().isAvailable() == false){
                continue; 
            }
            
            OrderDish newOrderDish = OrderDish.builder()
                    .dish(userDish.getDish())
                    .quantity(userDish.getQuantity())
                    .price(userDish.getDish().getPrice())
                    .order(newOrder)
                    .build();

            orderDishes.add(newOrderDish);
        }

        newOrder.setTotalPrice(totalPrice(orderDishes));
        orderDishRepository.saveAll(orderDishes);

        // Xoá các user dish đã đặt (userdish ở đây đóng vai trò như Cart)
        userDishRepository.deleteAll(allUserDishByUserId);

        return orderRepository.save(newOrder);
    }

    private Float totalPrice(List<OrderDish> orderDishes){
        float totalPrice = 0.0f;
        for (OrderDish orderDish: orderDishes){
            totalPrice += orderDish.getPrice() * orderDish.getQuantity();
        }

        return totalPrice;
    }

    @Override
    public List<OrderDishResponseDTO> getAllOrderItems(Integer orderId) {
        List<OrderDish> orderDishes = orderDishRepository.findByOrder_Id(orderId);

        if (orderDishes.isEmpty()){
            throw new RuntimeException("Order không tồn tại");
        }



        return orderDishes.stream().map(orderDish -> {
            Dish dish = orderDish.getDish();

            String imageUrl = imageRepository
                    .findFirstByDish_IdOrderByIdAsc(dish.getId())
                    .map(Image::getUrl)
                    .orElse("https://statics.vinpearl.com/com-tam-da-nang-4_1710137440.jpg");

            return OrderDishResponseDTO.builder()
                    .id(orderDish.getId())
                    .dishId(dish.getId())
                    .dishName(dish.getName())
                    .quantity(orderDish.getQuantity())
                    .price(orderDish.getPrice())
                    .imageUrl(imageUrl)
                    .restaurantName(dish.getRestaurant().getName())
                    .reviewed(orderDish.getReview() != null)
                    .build();
        }).toList();

    }
}
