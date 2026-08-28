package com.example.foodie.identity.address.service;

import com.example.foodie.identity.address.dto.request.AddressDTO;
import com.example.foodie.identity.address.entity.Address;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface AddressService {
    public Address addAddressByUserId(Authentication authentication, AddressDTO addressDTO);
    public List<Address> getAllAddressesByUser(Authentication authentication);
    public void deleteAddressById(Authentication authentication, Integer addressId);
    public AddressDTO updateAddress(Authentication authentication, Integer addressId, AddressDTO addressDTO);
    public List<Address> getAllAddressesByUserId(Integer userId);
}
