package com.example.foodie.identity.admin.dashboard.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardTrendPointDTO {
    private String date; // định dạng yyyy-MM-dd
    private double revenue;
    private long orderCount;
    private long newCustomerCount;
}
