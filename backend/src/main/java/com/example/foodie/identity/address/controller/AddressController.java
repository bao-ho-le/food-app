package com.example.foodie.identity.address.controller;

import com.example.foodie.identity.address.dto.request.AddressDTO;
import com.example.foodie.identity.address.entity.Address;
import com.example.foodie.identity.address.service.AddressService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("${api.prefix}/address")
@AllArgsConstructor
public class AddressController implements AddressControllerDocs {
    private final AddressService addressService;

    @Override
    @GetMapping
    public ResponseEntity<List<Address>> getAll(){
        return ResponseEntity.ok(addressService.getAll());
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<Address> getById(@PathVariable Integer id){
        return ResponseEntity.ok(addressService.getById(id));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Integer id){
        addressService.deleteById(id);

        return ResponseEntity.ok().build();
    }

    @Override
    @PostMapping("/user")
    public ResponseEntity<Address> addAddressByUserId(Authentication authentication,@Valid @RequestBody AddressDTO addressDTO) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(addressService.addAddressByUserId(authentication, addressDTO));
    }

    @Override
    @GetMapping("/user")
    public ResponseEntity<List<Address>> getAllAddressesByUserId(Authentication authentication) {
        return ResponseEntity.ok(addressService.getAllAddressesByUser(authentication));
    }

    @Override
    @DeleteMapping("/user/{address_id}")
    public ResponseEntity<String> deleteAddress(
            @PathVariable(name="address_id") Integer addressId) {
        addressService.deleteAddressById(addressId);

        return ResponseEntity.ok("Xoá địa chỉ thành công");
    }

    @Override
    @PutMapping("/user/{address_id}")
    public ResponseEntity<AddressDTO> updateAddress(Authentication authentication,
                                            @PathVariable(name="address_id") Integer addressId,
                                            @Valid @RequestBody AddressDTO addressDTO) {
        return ResponseEntity.ok(addressService.updateAddress(authentication, addressId, addressDTO));
    }

}
