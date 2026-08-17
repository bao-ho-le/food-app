package com.example.foodie.order.helper;

import org.springframework.stereotype.Component;

@Component
public class OrderHelper {

    public void validateOrderId(Integer orderId) {
        if (orderId == null || orderId <= 0) {
            throw new RuntimeException("Id đơn hàng không hợp lệ");
        }
    }

    public void validateAddressId(Integer addressId) {
        if (addressId == null || addressId <= 0) {
            throw new RuntimeException("Id địa chỉ không hợp lệ");
        }
    }
}
