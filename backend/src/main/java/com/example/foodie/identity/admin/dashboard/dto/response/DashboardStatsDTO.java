package com.example.foodie.identity.admin.dashboard.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardStatsDTO {
    private long totalUsers;
    private double totalUsersChangePercent;
    private long totalStores;
    private double totalStoresChangePercent;
    private long totalOrders;
    private double totalOrdersChangePercent;
    private double totalRevenue;
    private double totalRevenueChangePercent;
}
