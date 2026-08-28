package com.example.foodie.identity.address.service;

import com.example.foodie.identity.address.dto.request.AddressDTO;
import com.example.foodie.identity.address.entity.Address;
import com.example.foodie.identity.address.helper.AddressHelper;
import com.example.foodie.identity.address.mapper.AddressMapper;
import com.example.foodie.identity.address.repository.AddressRepository;
import com.example.foodie.common.exception.ErrorCode;
import com.example.foodie.common.exception.business_exception.IdentityException;
import com.example.foodie.identity.user.entity.User;
import com.example.foodie.identity.user.helper.UserHelper;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {
    private final AddressRepository addressRepository;
    private final AddressHelper addressHelper;
    private final AddressMapper addressMapper;
    private final UserHelper userHelper;

    public AddressServiceImpl(AddressRepository addressRepository,
                              AddressHelper addressHelper,
                              AddressMapper addressMapper,
                              UserHelper userHelper) {
        this.addressRepository = addressRepository;
        this.addressHelper = addressHelper;
        this.addressMapper = addressMapper;
        this.userHelper = userHelper;
    }

    @Override
    public Address addAddressByUserId(Authentication authentication, AddressDTO addressDTO){
        addressHelper.validateAddressRequest(addressDTO);

        User user = userHelper.getUserFromAuthentication(authentication);

        Address defaultAddress = findDefaultAddressAndRejectDuplicate(user.getId(), addressDTO.getAddress());

        if (Boolean.TRUE.equals(addressDTO.getIsDefault()) && defaultAddress != null) {
            defaultAddress.setIsDefault(false);
            addressRepository.save(defaultAddress);
        }

        return addressRepository.save(addressMapper.toEntity(addressDTO, user));
    }

    @Override
    public List<Address> getAllAddressesByUser(Authentication authentication){
        User user = userHelper.getUserFromAuthentication(authentication);

        return addressRepository.findAllByUser_Id(user.getId());
    }

    @Override
    public List<Address> getAllAddressesByUserId(Integer userId){
        return addressRepository.findAllByUser_Id(userId);
    }

    @Override
    public void deleteAddressById(Authentication authentication, Integer addressId){
        addressHelper.validateAddressId(addressId);

        User user = userHelper.getUserFromAuthentication(authentication);

        if (!addressRepository.existsByIdAndUser_Id(addressId, user.getId())) {
            throw new IdentityException(ErrorCode.ADDRESS_NOT_FOUND);
        }
        addressRepository.deleteByIdAndUser_Id(addressId, user.getId());
    }

    @Override
    public AddressDTO updateAddress(Authentication authentication, Integer addressId, AddressDTO addressDTO){
        addressHelper.validateAddressId(addressId);
        addressHelper.validateAddressRequest(addressDTO);

        User user = userHelper.getUserFromAuthentication(authentication);
        Address address = addressRepository.findByIdAndUser_Id(addressId, user.getId())
                .orElseThrow(() -> new IdentityException(ErrorCode.ADDRESS_NOT_FOUND));

        if (Boolean.TRUE.equals(addressDTO.getIsDefault())) {
            unsetCurrentDefaultAddress(user.getId(), address.getId());
        }

        address.setAddress(addressDTO.getAddress());
        address.setIsDefault(addressDTO.getIsDefault());
        addressRepository.save(address);

        return addressMapper.toDto(address);
    }

    // Helper

    private Address findDefaultAddressAndRejectDuplicate(Integer userId, String newAddress) {
        Address defaultAddress = null;

        for (Address address : addressRepository.findByUser_Id(userId)) {
            if (address.getAddress().equals(newAddress)) {
                throw new IdentityException(ErrorCode.ADDRESS_ALREADY_EXISTS);
            }
            if (address.getIsDefault()) {
                defaultAddress = address;
            }
        }
        return defaultAddress;
    }

    private void unsetCurrentDefaultAddress(Integer userId, Integer keptAddressId) {
        Address currentDefaultAddress = addressRepository.findByUser_IdAndIsDefault(userId, true)
                .orElse(null);

        if (currentDefaultAddress != null && !currentDefaultAddress.getId().equals(keptAddressId)) {
            currentDefaultAddress.setIsDefault(false);
            addressRepository.save(currentDefaultAddress);
        }
    }
}
