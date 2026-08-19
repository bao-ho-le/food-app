package com.example.foodie.identity.admin.dashboard.controller;

import com.example.foodie.identity.admin.dashboard.dto.response.DashboardStatsDTO;
import com.example.foodie.identity.admin.dashboard.dto.response.DashboardTrendPointDTO;
import com.example.foodie.identity.admin.dashboard.dto.response.TopProductDTO;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

import static com.example.foodie.common.config.OpenApiConfig.BEARER_SECURITY_SCHEME;

@Tag(name = "Admin - Dashboard", description = "Thống kê tổng quan cho trang chủ admin (chỉ ADMIN)")
@SecurityRequirement(name = BEARER_SECURITY_SCHEME)
public interface AdminDashboardControllerDocs {

    ResponseEntity<DashboardStatsDTO> getStats();

    ResponseEntity<List<DashboardTrendPointDTO>> getTrend(@RequestParam(defaultValue = "7") int days);

    ResponseEntity<List<TopProductDTO>> getTopProducts();
}
