package com.example.foodie.identity.address.helper;

import com.example.foodie.common.exception.ErrorCode;
import com.example.foodie.common.exception.business_exception.IdentityException;
import com.example.foodie.identity.address.dto.request.AddressDTO;
import org.springframework.stereotype.Component;

@Component
public class AddressHelper {

    private static final int MAX_ADDRESS_LENGTH = 255;

    public void validateAddressId(Integer addressId) {
        if (addressId == null) {
            throw new IdentityException(ErrorCode.ADDRESS_ID_REQUIRED);
        }
        if (addressId <= 0) {
            throw new IdentityException(ErrorCode.ADDRESS_ID_INVALID);
        }
    }

    public void validateAddressRequest(AddressDTO addressDTO) {
        if (addressDTO == null) {
            throw new IdentityException(ErrorCode.ADDRESS_REQUEST_REQUIRED);
        }
        if (addressDTO.getAddress() == null || addressDTO.getAddress().isBlank()) {
            throw new IdentityException(ErrorCode.ADDRESS_LINE_REQUIRED);
        }
        if (addressDTO.getAddress().length() > MAX_ADDRESS_LENGTH) {
            throw new IdentityException(ErrorCode.ADDRESS_LINE_TOO_LONG);
        }
    }
}
