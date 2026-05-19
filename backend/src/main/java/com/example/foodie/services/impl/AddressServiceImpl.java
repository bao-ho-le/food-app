package com.example.foodie.services.impl;

import com.example.foodie.dtos.AddressDTO;
import com.example.foodie.models.Address;
import com.example.foodie.models.User;
import com.example.foodie.repos.AddressRepository;
import com.example.foodie.repos.UserRepository;
import com.example.foodie.services.interfaces.AddressService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AddressServiceImpl extends BaseServiceImpl<Address> implements AddressService {
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public AddressServiceImpl(AddressRepository addressRepository, UserRepository userRepository) {
        super(addressRepository, Address.class);
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    private User getUserFromAuthentication(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
    }

    @Override
    public Address addAddressByUserId(Authentication authentication, AddressDTO addressDTO){
        User user = getUserFromAuthentication(authentication);

        List<Address> allAddressesExisting = addressRepository.findByUser_Id(user.getId());
        Address defaultAddress = null;

        for (Address address: allAddressesExisting){
            if (address.getAddress().equals(addressDTO.getAddress()))
                throw new RuntimeException("User đã có địa chỉ này rồi");
            if (address.getIsDefault())
                defaultAddress = address;
        }

        if (addressDTO.getIsDefault() != null && addressDTO.getIsDefault()) {
            if (defaultAddress != null) {
                defaultAddress.setIsDefault(false);
                addressRepository.save(defaultAddress);
            }
        }

        Address newAddress = Address.builder()
                .address(addressDTO.getAddress())
                .user(user)
                .isDefault(addressDTO.getIsDefault())
                .build();

        return addressRepository.save(newAddress);
    }

    @Override
    public List<Address> getAllAddressesByUser(Authentication authentication){
        User user = getUserFromAuthentication(authentication);

        List<Address> allAddresses = addressRepository.findAllByUser_Id(user.getId());
        if (allAddresses.isEmpty()){
            throw new RuntimeException("User không có địa chỉ nào");
        }
        return allAddresses;
    }

    @Override
    public void deleteAddressById(Integer addressId){
        if (!addressRepository.existsById(addressId)) {
            throw new RuntimeException("Địa chỉ không tồn tại");
        }
        addressRepository.deleteById(addressId);
    }

    @Override
    public AddressDTO updateAddress(Authentication authentication, Integer addressId, AddressDTO addressDTO){
        User user = getUserFromAuthentication(authentication);
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại"));

        if (addressDTO.getIsDefault() != null && addressDTO.getIsDefault()) {
            Address currentDefaultAddress = addressRepository.findByUser_IdAndIsDefault(user.getId(), true)
                    .orElse(null);
            if (currentDefaultAddress != null && !currentDefaultAddress.getId().equals(address.getId())) {
                currentDefaultAddress.setIsDefault(false);
                addressRepository.save(currentDefaultAddress);
            }
        }

        address.setAddress(addressDTO.getAddress());
        address.setIsDefault(addressDTO.getIsDefault());
        addressRepository.save(address);

        return AddressDTO.builder()
                .address(addressDTO.getAddress())
                .isDefault(addressDTO.getIsDefault())
                .build();

    }


}
