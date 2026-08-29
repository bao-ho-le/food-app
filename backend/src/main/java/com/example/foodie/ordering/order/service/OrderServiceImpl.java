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
import com.example.foodie.ordering.order.dto.response.OrderResponseDTO;
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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    public List<OrderResponseDTO> getAll(){
        return orderRepository.findAll().stream().map(orderMapper::toResponse).toList();
    }

    @Override
    public OrderResponseDTO getById(Integer id){
        return orderMapper.toResponse(getOrderOrThrow(id));
    }

    @Override
    public void deleteById(Integer id){
        Order order = getOrderOrThrow(id);

        // Đơn đã giao phải được giữ lại cho audit/reporting; xoá dữ liệu cần cơ
        // chế archival riêng, không phải DELETE thông thường.
        if (order.getStatus() == Status.DELIVERED) {
            throw new OrderingException(ErrorCode.ORDER_DELETE_FORBIDDEN);
        }
        orderRepository.deleteById(id);
    }

    @Override
    public List<OrderResponseDTO> getAllOrdersByUserId(Authentication authentication){
        User user = userHelper.getUserFromAuthentication(authentication);

        return orderRepository.findAllByUser_Id(user.getId()).stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Transactional
    @Override
    public OrderResponseDTO createOrder(Authentication authentication, Integer addressId){
        orderHelper.validateAddressId(addressId);

        User user = userHelper.getUserFromAuthentication(authentication);

        List<UserDish> allUserDishByUserId = userDishRepository.findAllByUser_Id(user.getId());

        if(allUserDishByUserId.isEmpty()){
            throw new OrderingException(ErrorCode.ORDER_CART_EMPTY);
        }

        Address address = addressRepository.findByIdAndUser_Id(addressId, user.getId())
                .orElseThrow(() -> new OrderingException(ErrorCode.ADDRESS_NOT_FOUND));

        // Khoá từng dish liên quan (SELECT ... FOR UPDATE) theo thứ tự id tăng dần để
        // tránh deadlock khi nhiều đơn hàng song song cùng đụng vào tập dish giao nhau.
        Map<Integer, Dish> lockedDishesById = lockDishesForUpdate(
                allUserDishByUserId.stream().map(userDish -> userDish.getDish().getId()).toList());

        Order newOrder = orderRepository.save(orderMapper.toEntity(user, address.getAddress()));

        // All-or-nothing: nếu bất kỳ món nào không khả dụng hoặc không đủ tồn kho,
        // buildOrderDishes ném lỗi và @Transactional rollback toàn bộ — không tạo
        // đơn một phần, không trừ kho các món khác, không xoá giỏ hàng.
        List<OrderDish> orderDishes = buildOrderDishes(allUserDishByUserId, lockedDishesById, newOrder);

        newOrder.setTotalPrice(totalPrice(orderDishes));
        orderDishRepository.saveAll(orderDishes);
        dishRepository.saveAll(lockedDishesById.values());

        // Xoá các user dish đã đặt (userdish ở đây đóng vai trò như Cart)
        userDishRepository.deleteAll(allUserDishByUserId);

        return orderMapper.toResponse(orderRepository.save(newOrder));
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
    public OrderResponseDTO updateStatus(Integer orderId, Status newStatus) {

        orderHelper.validateOrderId(orderId);

        Order order = getOrderOrThrow(orderId);

        orderHelper.validateStatusTransition(order.getStatus(), newStatus);
        restoreStockIfCancelling(order, newStatus);
        order.setStatus(newStatus);

        try {
            return orderMapper.toResponse(orderRepository.saveAndFlush(order));
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new OrderingException(ErrorCode.ORDER_STATUS_CONFLICT, e);
        }
    }

    @Override
    @Transactional
    public OrderResponseDTO cancelOwnOrder(Authentication authentication, Integer orderId) {

        Order order = getOwnOrderOrThrow(authentication, orderId);
        orderHelper.validateStatusTransition(order.getStatus(), Status.CANCELLED);
        restoreStockIfCancelling(order, Status.CANCELLED);
        order.setStatus(Status.CANCELLED);

        try {
            return orderMapper.toResponse(orderRepository.saveAndFlush(order));
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new OrderingException(ErrorCode.ORDER_STATUS_CONFLICT, e);
        }
    }

    @Override
    @Transactional
    public OrderResponseDTO confirmOwnOrderReceived(Authentication authentication, Integer orderId) {

        Order order = getOwnOrderOrThrow(authentication, orderId);
        orderHelper.validateStatusTransition(order.getStatus(), Status.DELIVERED);
        order.setStatus(Status.DELIVERED);

        try {
            return orderMapper.toResponse(orderRepository.saveAndFlush(order));
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new OrderingException(ErrorCode.ORDER_STATUS_CONFLICT, e);
        }
    }



    // Helper

    private Order getOrderOrThrow(Integer orderId) {
        orderHelper.validateOrderId(orderId);
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderingException(ErrorCode.ORDER_NOT_FOUND));
    }

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

    private Map<Integer, Dish> lockDishesForUpdate(Collection<Integer> dishIds) {

        // Sort giúp giải quyết deadlock
        List<Integer> sortedDishIds = dishIds.stream().distinct().sorted().toList();

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

            if (!dish.isAvailable()){
                throw new CatalogException(ErrorCode.DISH_NOT_AVAILABLE);
            }

            if (dish.getStockQuantity() < userDish.getQuantity()){
                throw new CatalogException(ErrorCode.DISH_OUT_OF_STOCK);
            }

            dish.setStockQuantity(dish.getStockQuantity() - userDish.getQuantity());
            orderDishes.add(orderMapper.toOrderDish(userDish, order));
        }
        return orderDishes;
    }

    // Do máy trạng thái chỉ cho phép CANCELLED xuất phát từ PENDING/PREPARING
    // (OrderHelper.validateStatusTransition), món ăn ở đây chắc chắn chưa từng
    // được chế biến — hoàn kho luôn là đúng, không cần phân biệt thêm.
    private void restoreStockIfCancelling(Order order, Status newStatus) {
        if (newStatus != Status.CANCELLED) {
            return;
        }

        List<OrderDish> orderDishes = orderDishRepository.findByOrder_Id(order.getId());
        if (orderDishes.isEmpty()) {
            return;
        }

        Map<Integer, Dish> lockedDishesById = lockDishesForUpdate(
                orderDishes.stream().map(orderDish -> orderDish.getDish().getId()).toList());

        for (OrderDish orderDish : orderDishes) {
            Dish dish = lockedDishesById.get(orderDish.getDish().getId());
            dish.setStockQuantity(dish.getStockQuantity() + orderDish.getQuantity());
        }
        dishRepository.saveAll(lockedDishesById.values());
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
