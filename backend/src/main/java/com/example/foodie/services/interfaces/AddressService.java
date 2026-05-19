package com.example.foodie.services.interfaces;

import com.example.foodie.dtos.AddressDTO;
import com.example.foodie.models.Address;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface AddressService extends BaseService<Address>{
    public Address addAddressByUserId(Authentication authentication, AddressDTO addressDTO);
    public List<Address> getAllAddressesByUser(Authentication authentication);
    public void deleteAddressById(Integer addressId);
    public AddressDTO updateAddress(Authentication authentication, Integer addressId, AddressDTO addressDTO);
}
