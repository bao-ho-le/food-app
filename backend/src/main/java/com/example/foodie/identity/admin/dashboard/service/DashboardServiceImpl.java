package com.example.foodie.identity.admin.dashboard.service;

import com.example.foodie.catalog.dish.dto.response.DishDTO;
import com.example.foodie.catalog.dish.service.DishService;
import com.example.foodie.catalog.restaurant.repository.RestaurantRepository;
import com.example.foodie.common.exception.ErrorCode;
import com.example.foodie.common.exception.business_exception.IdentityException;
import com.example.foodie.identity.admin.dashboard.dto.response.DashboardStatsDTO;
import com.example.foodie.identity.admin.dashboard.dto.response.DashboardTrendPointDTO;
import com.example.foodie.identity.admin.dashboard.dto.response.TopProductDTO;
import com.example.foodie.identity.admin.dashboard.mapper.DashboardMapper;
import com.example.foodie.identity.user.entity.User;
import com.example.foodie.identity.user.repository.UserRepository;
import com.example.foodie.ordering.order.entity.Order;
import com.example.foodie.ordering.order.enums.Status;
import com.example.foodie.ordering.order.repository.OrderDishRepository;
import com.example.foodie.ordering.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final List<Integer> ALLOWED_TREND_PERIOD_DAYS = List.of(7, 14, 30);
    private static final int TOP_PRODUCTS_LIMIT = 5;

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final OrderDishRepository orderDishRepository;
    private final DishService dishService;
    private final DashboardMapper dashboardMapper;

    @Override
    public DashboardStatsDTO getDashboardStats() {
        Instant now = Instant.now();
        Instant currentMonthStart = startOfCurrentMonth();
        Instant previousMonthStart = startOfPreviousMonth();

        long currentMonthUserCount = userRepository.countByCreatedAtBetween(currentMonthStart, now);
        long previousMonthUserCount = userRepository.countByCreatedAtBetween(previousMonthStart, currentMonthStart);

        long currentMonthStoreCount = restaurantRepository.countByCreatedAtBetween(currentMonthStart, now);
        long previousMonthStoreCount = restaurantRepository.countByCreatedAtBetween(previousMonthStart, currentMonthStart);

        long currentMonthOrderCount = orderRepository.countByCreatedAtBetween(currentMonthStart, now);
        long previousMonthOrderCount = orderRepository.countByCreatedAtBetween(previousMonthStart, currentMonthStart);

        double currentMonthRevenueAmount = sumDeliveredRevenueInRange(currentMonthStart, now);
        double previousMonthRevenueAmount = sumDeliveredRevenueInRange(previousMonthStart, currentMonthStart);

        return dashboardMapper.toDashboardStatsDto(
                userRepository.count(), calculatePercentChange(currentMonthUserCount, previousMonthUserCount),
                restaurantRepository.countActiveRestaurants(), calculatePercentChange(currentMonthStoreCount, previousMonthStoreCount),
                orderRepository.count(), calculatePercentChange(currentMonthOrderCount, previousMonthOrderCount),
                sumDeliveredRevenueInRange(Instant.EPOCH, now), calculatePercentChange(currentMonthRevenueAmount, previousMonthRevenueAmount)
        );
    }

    @Override
    public List<DashboardTrendPointDTO> getRevenueTrend(int days) {
        if (!ALLOWED_TREND_PERIOD_DAYS.contains(days)) {
            throw new IdentityException(ErrorCode.DASHBOARD_INVALID_TREND_PERIOD);
        }

        Instant rangeStart = Instant.now().minus(days, ChronoUnit.DAYS);
        List<Order> ordersInRange = orderRepository.findByCreatedAtBetween(rangeStart, Instant.now());
        List<User> usersInRange = userRepository.findByCreatedAtBetween(rangeStart, Instant.now());

        Map<LocalDate, Double> revenueByDay = new TreeMap<>();
        Map<LocalDate, Long> orderCountByDay = new TreeMap<>();
        Map<LocalDate, Long> newCustomerCountByDay = new TreeMap<>();
        for (int dayOffset = days - 1; dayOffset >= 0; dayOffset--) {
            LocalDate day = LocalDate.now().minusDays(dayOffset);
            revenueByDay.put(day, 0.0);
            orderCountByDay.put(day, 0L);
            newCustomerCountByDay.put(day, 0L);
        }

        for (Order order : ordersInRange) {
            LocalDate orderDay = LocalDate.ofInstant(order.getCreatedAt(), ZoneId.systemDefault());
            orderCountByDay.merge(orderDay, 1L, Long::sum);
            if (order.getStatus() == Status.DELIVERED) {
                revenueByDay.merge(orderDay, order.getTotalPrice().doubleValue(), Double::sum);
            }
        }
        for (User user : usersInRange) {
            LocalDate signupDay = LocalDate.ofInstant(user.getCreatedAt(), ZoneId.systemDefault());
            newCustomerCountByDay.merge(signupDay, 1L, Long::sum);
        }

        List<DashboardTrendPointDTO> trendPoints = new ArrayList<>();
        for (LocalDate day : revenueByDay.keySet()) {
            trendPoints.add(dashboardMapper.toTrendPointDto(day, revenueByDay.get(day), orderCountByDay.get(day), newCustomerCountByDay.get(day)));
        }
        return trendPoints;
    }

    @Override
    public List<TopProductDTO> getTopProducts() {
        List<Object[]> dishIdToQuantitySoldRows = orderDishRepository.sumQuantityByDishForOrderStatus(Status.DELIVERED);

        Map<Integer, Long> quantitySoldByDishId = new LinkedHashMap<>();
        for (Object[] row : dishIdToQuantitySoldRows) {
            if (quantitySoldByDishId.size() >= TOP_PRODUCTS_LIMIT) break;
            Integer dishId = (Integer) row[0];
            Long quantitySold = (Long) row[1];
            quantitySoldByDishId.put(dishId, quantitySold);
        }

        Map<Integer, DishDTO> dishById = new HashMap<>();
        for (DishDTO dish : dishService.getAllDishes()) {
            dishById.put(dish.getId(), dish);
        }

        List<TopProductDTO> topProducts = new ArrayList<>();
        for (Map.Entry<Integer, Long> entry : quantitySoldByDishId.entrySet()) {
            DishDTO matchingDish = dishById.get(entry.getKey());
            if (matchingDish != null) {
                topProducts.add(dashboardMapper.toTopProductDto(matchingDish, entry.getValue()));
            }
        }
        return topProducts;
    }

    // Helper

    private double sumDeliveredRevenueInRange(Instant rangeStart, Instant rangeEnd) {
        Float totalRevenueOrNull = orderRepository.sumTotalPriceByStatusAndCreatedAtBetween(Status.DELIVERED, rangeStart, rangeEnd);
        return totalRevenueOrNull != null ? totalRevenueOrNull.doubleValue() : 0.0;
    }

    private static double calculatePercentChange(double currentValue, double previousValue) {
        if (previousValue == 0) {
            return currentValue == 0 ? 0.0 : 100.0;
        }
        return ((currentValue - previousValue) / previousValue) * 100.0;
    }

    private static Instant startOfCurrentMonth() {
        return LocalDate.now().withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    private static Instant startOfPreviousMonth() {
        return LocalDate.now().minusMonths(1).withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
    }
}
