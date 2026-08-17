package com.example.foodie.ordering.order.helper;

import com.example.foodie.common.exception.ErrorCode;
import com.example.foodie.common.exception.business_exception.OrderingException;
import org.springframework.stereotype.Component;

@Component
public class OrderHelper {

    public void validateOrderId(Integer orderId) {
        if (orderId == null) {
            throw new OrderingException(ErrorCode.ORDER_ID_REQUIRED);
        }
        if (orderId <= 0) {
            throw new OrderingException(ErrorCode.ORDER_ID_INVALID);
        }
    }

    public void validateAddressId(Integer addressId) {
        if (addressId == null) {
            throw new OrderingException(ErrorCode.ADDRESS_ID_REQUIRED);
        }
        if (addressId <= 0) {
            throw new OrderingException(ErrorCode.ADDRESS_ID_INVALID);
        }
    }
}
