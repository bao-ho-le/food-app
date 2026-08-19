package com.example.foodie.identity.admin.dashboard.controller;

import com.example.foodie.identity.admin.dashboard.dto.response.DashboardStatsDTO;
import com.example.foodie.identity.admin.dashboard.dto.response.DashboardTrendPointDTO;
import com.example.foodie.identity.admin.dashboard.dto.response.TopProductDTO;
import com.example.foodie.identity.admin.dashboard.service.DashboardService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/admin/dashboard")
@AllArgsConstructor
public class AdminDashboardController implements AdminDashboardControllerDocs {

    private final DashboardService dashboardService;

    @Override
    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getStats() {
        return ResponseEntity.ok(dashboardService.getDashboardStats());
    }

    @Override
    @GetMapping("/trend")
    public ResponseEntity<List<DashboardTrendPointDTO>> getTrend(@RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(dashboardService.getRevenueTrend(days));
    }

    @Override
    @GetMapping("/top-products")
    public ResponseEntity<List<TopProductDTO>> getTopProducts() {
        return ResponseEntity.ok(dashboardService.getTopProducts());
    }
}
