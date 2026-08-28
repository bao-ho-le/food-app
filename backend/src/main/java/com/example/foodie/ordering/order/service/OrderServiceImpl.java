package com.example.foodie.ordering.order.service;

import com.example.foodie.catalog.dish.entity.Dish;
import com.example.foodie.catalog.dish.repository.DishRepository;
import com.example.foodie.identity.address.entity.Address;
import com.example.foodie.identity.address.repository.AddressRepository;
import com.example.foodie.common.exception.ErrorCode;
import com.example.foodie.common.exception.business_exception.CatalogException;
import com.example.foodie.common.exception.business_exception.OrderingException;
import com.example.foodie.catalog.image.entity.Image;
import com.example.foodie.catalog.image.repository.ImageRepository;
import com.example.foodie.ordering.order.dto.response.OrderDishResponseDTO;
import com.example.foodie.ordering.order.entity.Order;
import com.example.foodie.ordering.order.entity.OrderDish;
import com.example.foodie.ordering.order.enums.Status;
import com.example.foodie.ordering.order.helper.OrderHelper;
import com.example.foodie.ordering.order.mapper.OrderMapper;
import com.example.foodie.ordering.order.repository.OrderDishRepository;
import com.example.foodie.ordering.order.repository.OrderRepository;
import com.example.foodie.identity.user.entity.User;
import com.example.foodie.identity.user.helper.UserHelper;
import com.example.foodie.ordering.userdish.entity.UserDish;
import com.example.foodie.ordering.userdish.repository.UserDishRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private static final String DEFAULT_DISH_IMAGE_URL =
            "https://statics.vinpearl.com/com-tam-da-nang-4_1710137440.jpg";

    private final OrderRepository orderRepository;
    private final UserDishRepository userDishRepository;
    private final AddressRepository addressRepository;
    private final OrderDishRepository orderDishRepository;
    private final ImageRepository imageRepository;
    private final DishRepository dishRepository;
    private final OrderHelper orderHelper;
    private final OrderMapper orderMapper;
    private final UserHelper userHelper;


    @Override
    public List<Order> getAll(){
        return orderRepository.findAll();
    }

    @Override
    public Order getById(Integer id){
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderingException(ErrorCode.ORDER_NOT_FOUND));
    }

    @Override
    public void deleteById(Integer id){
        orderRepository.deleteById(id);
    }

    @Override
    public List<Order> getAllOrdersByUserId(Authentication authentication){
        User user = userHelper.getUserFromAuthentication(authentication);

        return orderRepository.findAllByUser_Id(user.getId());
    }

    @Transactional
    @Override
    public Order createOrder(Authentication authentication, Integer addressId){
        orderHelper.validateAddressId(addressId);

        User user = userHelper.getUserFromAuthentication(authentication);

        List<UserDish> allUserDishByUserId = userDishRepository.findAllByUser_Id(user.getId());

        if(allUserDishByUserId.isEmpty()){
            throw new OrderingException(ErrorCode.ORDER_CART_EMPTY);
        }

        Optional<Address> address = addressRepository.findById(addressId);

        if (address.isEmpty()){
            throw new OrderingException(ErrorCode.ADDRESS_NOT_FOUND);
        }

        // Khoá từng dish liên quan (SELECT ... FOR UPDATE) theo thứ tự id tăng dần để
        // tránh deadlock khi nhiều đơn hàng song song cùng đụng vào tập dish giao nhau.
        Map<Integer, Dish> lockedDishesById = lockDishesForUpdate(allUserDishByUserId);

        Order newOrder = orderRepository.save(orderMapper.toEntity(user, address.get().getAddress()));

        List<OrderDish> orderDishes = buildOrderDishes(allUserDishByUserId, lockedDishesById, newOrder);

        newOrder.setTotalPrice(totalPrice(orderDishes));
        orderDishRepository.saveAll(orderDishes);
        dishRepository.saveAll(lockedDishesById.values());

        // Xoá các user dish đã đặt (userdish ở đây đóng vai trò như Cart)
        userDishRepository.deleteAll(allUserDishByUserId);

        return orderRepository.save(newOrder);
    }

    @Override
    public List<OrderDishResponseDTO> getAllOrderItems(Integer orderId) {
        orderHelper.validateOrderId(orderId);

        List<OrderDish> orderDishes = orderDishRepository.findByOrder_Id(orderId);

        if (orderDishes.isEmpty()){
            throw new OrderingException(ErrorCode.ORDER_NOT_FOUND);
        }

        return orderDishes.stream()
                .map(orderDish -> orderMapper.toOrderDishResponse(
                        orderDish,
                        resolveImageUrl(orderDish.getDish().getId())))
                .toList();
    }

    @Override
    public List<OrderDishResponseDTO> getOwnOrderItems(Authentication authentication, Integer orderId) {
        Order order = getOwnOrderOrThrow(authentication, orderId);

        List<OrderDish> orderDishes = orderDishRepository.findByOrder_Id(order.getId());

        if (orderDishes.isEmpty()){
            throw new OrderingException(ErrorCode.ORDER_NOT_FOUND);
        }

        return orderDishes.stream()
                .map(orderDish -> orderMapper.toOrderDishResponse(
                        orderDish,
                        resolveImageUrl(orderDish.getDish().getId())))
                .toList();
    }

    @Override
    @Transactional
    public Order updateStatus(Integer orderId, Status newStatus) {

        orderHelper.validateOrderId(orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderingException(ErrorCode.ORDER_NOT_FOUND));

        orderHelper.validateStatusTransition(order.getStatus(), newStatus);
        order.setStatus(newStatus);

        try {
            return orderRepository.saveAndFlush(order);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new OrderingException(ErrorCode.ORDER_STATUS_CONFLICT, e);
        }
    }

    @Override
    @Transactional
    public Order cancelOwnOrder(Authentication authentication, Integer orderId) {

        Order order = getOwnOrderOrThrow(authentication, orderId);
        orderHelper.validateStatusTransition(order.getStatus(), Status.CANCELLED);
        order.setStatus(Status.CANCELLED);

        try {
            return orderRepository.saveAndFlush(order);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new OrderingException(ErrorCode.ORDER_STATUS_CONFLICT, e);
        }
    }

    @Override
    @Transactional
    public Order confirmOwnOrderReceived(Authentication authentication, Integer orderId) {

        Order order = getOwnOrderOrThrow(authentication, orderId);
        orderHelper.validateStatusTransition(order.getStatus(), Status.DELIVERED);
        order.setStatus(Status.DELIVERED);

        try {
            return orderRepository.saveAndFlush(order);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new OrderingException(ErrorCode.ORDER_STATUS_CONFLICT, e);
        }
    }



    // Helper

    private Order getOwnOrderOrThrow(Authentication authentication, Integer orderId) {
        orderHelper.validateOrderId(orderId);
        User user = userHelper.getUserFromAuthentication(authentication);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderingException(ErrorCode.ORDER_NOT_FOUND));
        if (!order.getUser().getId().equals(user.getId())) {
            throw new OrderingException(ErrorCode.ORDER_NOT_OWNER);
        }
        return order;
    }

    private Map<Integer, Dish> lockDishesForUpdate(List<UserDish> userDishes) {

        // Sort giúp giải quyết deadlock
        List<Integer> sortedDishIds = userDishes.stream()
                .map(userDish -> userDish.getDish().getId())
                .distinct()
                .sorted()
                .toList();

        Map<Integer, Dish> lockedDishesById = new LinkedHashMap<>();
        for (Integer dishId : sortedDishIds) {
            Dish dish = dishRepository.findByIdForUpdate(dishId)
                    .orElseThrow(() -> new CatalogException(ErrorCode.DISH_NOT_FOUND));
            lockedDishesById.put(dishId, dish);
        }
        return lockedDishesById;
    }

    private List<OrderDish> buildOrderDishes(List<UserDish> userDishes, Map<Integer, Dish> lockedDishesById, Order order) {
        List<OrderDish> orderDishes = new ArrayList<>();

        for (UserDish userDish : userDishes){
            Dish dish = lockedDishesById.get(userDish.getDish().getId());

            if (userDish.getQuantity() <= 0 || !dish.isAvailable()){
                continue;
            }

            if (dish.getStockQuantity() < userDish.getQuantity()){
                throw new CatalogException(ErrorCode.DISH_OUT_OF_STOCK);
            }

            dish.setStockQuantity(dish.getStockQuantity() - userDish.getQuantity());
            orderDishes.add(orderMapper.toOrderDish(userDish, order));
        }
        return orderDishes;
    }

    private Float totalPrice(List<OrderDish> orderDishes){
        float totalPrice = 0.0f;
        for (OrderDish orderDish: orderDishes){
            totalPrice += orderDish.getPrice() * orderDish.getQuantity();
        }

        return totalPrice;
    }

    private String resolveImageUrl(Integer dishId) {
        return imageRepository.findFirstByDish_IdOrderByIdAsc(dishId)
                .map(Image::getUrl)
                .orElse(DEFAULT_DISH_IMAGE_URL);
    }
}
