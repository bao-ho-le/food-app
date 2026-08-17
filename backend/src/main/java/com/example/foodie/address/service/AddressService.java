package com.example.foodie.address.service;

import com.example.foodie.common.base.BaseService;
import com.example.foodie.address.dto.request.AddressDTO;
import com.example.foodie.address.entity.Address;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface AddressService extends BaseService<Address>{
    public Address addAddressByUserId(Authentication authentication, AddressDTO addressDTO);
    public List<Address> getAllAddressesByUser(Authentication authentication);
    public void deleteAddressById(Integer addressId);
    public AddressDTO updateAddress(Authentication authentication, Integer addressId, AddressDTO addressDTO);
}
