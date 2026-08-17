package com.example.foodie.identity.address.mapper;

import com.example.foodie.identity.address.dto.request.AddressDTO;
import com.example.foodie.identity.address.entity.Address;
import com.example.foodie.identity.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper {

    public Address toEntity(AddressDTO addressDTO, User user) {
        return Address.builder()
                .address(addressDTO.getAddress())
                .user(user)
                .isDefault(addressDTO.getIsDefault())
                .build();
    }

    public AddressDTO toDto(Address address) {
        return AddressDTO.builder()
                .address(address.getAddress())
                .isDefault(address.getIsDefault())
                .build();
    }
}
