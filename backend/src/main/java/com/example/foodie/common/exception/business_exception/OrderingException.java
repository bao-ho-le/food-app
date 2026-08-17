package com.example.foodie.common.exception.business_exception;

import com.example.foodie.common.exception.ErrorCode;

public class OrderingException extends BusinessException {
    public OrderingException(ErrorCode errorCode) {
        super(errorCode);
    }

    public OrderingException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, errorCode.getMessage(), cause);
    }

    public OrderingException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
