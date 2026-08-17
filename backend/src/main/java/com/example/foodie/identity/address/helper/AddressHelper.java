package com.example.foodie.identity.address.helper;

import com.example.foodie.identity.address.dto.request.AddressDTO;
import org.springframework.stereotype.Component;

@Component
public class AddressHelper {

    private static final int MAX_ADDRESS_LENGTH = 255;

    public void validateAddressId(Integer addressId) {
        if (addressId == null || addressId <= 0) {
            throw new RuntimeException("Id địa chỉ không hợp lệ");
        }
    }

    public void validateAddressRequest(AddressDTO addressDTO) {
        if (addressDTO == null) {
            throw new RuntimeException("Thông tin địa chỉ không được để trống");
        }
        if (addressDTO.getAddress() == null || addressDTO.getAddress().isBlank()) {
            throw new RuntimeException("Địa chỉ không được để trống");
        }
        if (addressDTO.getAddress().length() > MAX_ADDRESS_LENGTH) {
            throw new RuntimeException("Địa chỉ không được dài quá " + MAX_ADDRESS_LENGTH + " ký tự");
        }
    }
}
