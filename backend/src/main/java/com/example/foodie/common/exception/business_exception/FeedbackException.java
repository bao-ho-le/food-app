package com.example.foodie.common.exception.business_exception;

import com.example.foodie.common.exception.ErrorCode;

public class FeedbackException extends BusinessException {
    public FeedbackException(ErrorCode errorCode) {
        super(errorCode);
    }

    public FeedbackException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, errorCode.getMessage(), cause);
    }

    public FeedbackException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
