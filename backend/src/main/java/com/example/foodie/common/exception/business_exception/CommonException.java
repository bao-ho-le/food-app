package com.example.foodie.common.exception.business_exception;

import com.example.foodie.common.exception.ErrorCode;

public class CommonException  extends BusinessException {
    public CommonException(ErrorCode errorCode) {
        super(errorCode);
    }

    public CommonException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, errorCode.getMessage(), cause);
    }

    public CommonException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
