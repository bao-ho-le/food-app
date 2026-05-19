package com.example.foodie.controllers;

import com.example.foodie.dtos.AddressDTO;
import com.example.foodie.models.Address;
import com.example.foodie.services.interfaces.AddressService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("${api.prefix}/address")
public class AddressController extends BaseController<Address> {
    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        super(addressService);
        this.addressService = addressService;
    }

    @PostMapping("/user")
    public ResponseEntity<?> addAddressByUserId(Authentication authentication,@Valid @RequestBody AddressDTO addressDTO) {
        try {
            Address newAddress = addressService.addAddressByUserId(authentication, addressDTO);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(newAddress);

        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @GetMapping("/user")
    public ResponseEntity<?> getAllAddressesByUserId(Authentication authentication) {
        List<Address> allAddresses = addressService.getAllAddressesByUser(authentication);

        try{
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(allAddresses);

        } catch(RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @DeleteMapping("/user/{address_id}")
    public ResponseEntity<?> deleteAddress(@PathVariable(name="address_id") Integer addressId) {
        addressService.deleteAddressById(addressId);

        try{
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body("Xoá địa chỉ thành công");

        } catch(RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @PutMapping("/user/{address_id}")
    public ResponseEntity<?> updateAddress(Authentication authentication, 
                                            @PathVariable(name="address_id") Integer addressId,
                                            @Valid @RequestBody AddressDTO addressDTO) {
        AddressDTO addressDTORes = addressService.updateAddress(authentication, addressId, addressDTO);

        try{
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(addressDTORes);

        } catch(RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

}
