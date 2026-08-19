package com.example.foodie.identity.admin.dashboard.service;

import com.example.foodie.identity.admin.dashboard.dto.response.DashboardStatsDTO;
import com.example.foodie.identity.admin.dashboard.dto.response.DashboardTrendPointDTO;
import com.example.foodie.identity.admin.dashboard.dto.response.TopProductDTO;

import java.util.List;

public interface DashboardService {
    DashboardStatsDTO getDashboardStats();
    List<DashboardTrendPointDTO> getRevenueTrend(int days);
    List<TopProductDTO> getTopProducts();
}
