package com.example.foodie.common.exception.business_exception;

import com.example.foodie.common.exception.ErrorCode;

public class CatalogException extends BusinessException {
    public CatalogException(ErrorCode errorCode) {
        super(errorCode);
    }

    public CatalogException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, errorCode.getMessage(), cause);
    }

    public CatalogException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
