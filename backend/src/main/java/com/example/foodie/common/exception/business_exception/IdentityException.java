package com.example.foodie.common.exception.business_exception;

import com.example.foodie.common.exception.ErrorCode;

public class IdentityException extends BusinessException {
    public IdentityException(ErrorCode errorCode) {
        super(errorCode);
    }

    public IdentityException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, errorCode.getMessage(), cause);
    }

    public IdentityException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
