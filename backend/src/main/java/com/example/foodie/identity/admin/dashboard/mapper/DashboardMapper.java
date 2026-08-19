package com.example.foodie.identity.admin.dashboard.mapper;

import com.example.foodie.catalog.dish.dto.response.DishDTO;
import com.example.foodie.identity.admin.dashboard.dto.response.DashboardStatsDTO;
import com.example.foodie.identity.admin.dashboard.dto.response.DashboardTrendPointDTO;
import com.example.foodie.identity.admin.dashboard.dto.response.TopProductDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DashboardMapper {

    public DashboardStatsDTO toDashboardStatsDto(
            long totalUsers, double totalUsersChangePercent,
            long totalStores, double totalStoresChangePercent,
            long totalOrders, double totalOrdersChangePercent,
            double totalRevenue, double totalRevenueChangePercent) {
        return DashboardStatsDTO.builder()
                .totalUsers(totalUsers)
                .totalUsersChangePercent(totalUsersChangePercent)
                .totalStores(totalStores)
                .totalStoresChangePercent(totalStoresChangePercent)
                .totalOrders(totalOrders)
                .totalOrdersChangePercent(totalOrdersChangePercent)
                .totalRevenue(totalRevenue)
                .totalRevenueChangePercent(totalRevenueChangePercent)
                .build();
    }

    public DashboardTrendPointDTO toTrendPointDto(LocalDate date, double revenue, long orderCount, long newCustomerCount) {
        return DashboardTrendPointDTO.builder()
                .date(date.toString())
                .revenue(revenue)
                .orderCount(orderCount)
                .newCustomerCount(newCustomerCount)
                .build();
    }

    public TopProductDTO toTopProductDto(DishDTO dish, long quantitySold) {
        return TopProductDTO.builder()
                .dishId(dish.getId())
                .dishName(dish.getName())
                .imageUrl(dish.getUrl())
                .price(dish.getPrice())
                .quantitySold(quantitySold)
                .build();
    }
}
