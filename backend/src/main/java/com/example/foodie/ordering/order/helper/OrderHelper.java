package com.example.foodie.ordering.order.helper;

import com.example.foodie.common.exception.ErrorCode;
import com.example.foodie.common.exception.business_exception.OrderingException;
import com.example.foodie.ordering.order.enums.Status;
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

    public void validateStatusTransition(Status current, Status next) {
        boolean valid = switch (current) {
            case PENDING -> next == Status.PREPARING || next == Status.CANCELLED;
            case PREPARING -> next == Status.DELIVERED || next == Status.CANCELLED;
            default -> false; // DELIVERED, CANCELLED là trạng thái cuối
        };
        if (!valid) {
            throw new OrderingException(ErrorCode.ORDER_INVALID_STATUS_TRANSITION);
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
