package com.example.foodie.identity.admin.user.controller;

import com.example.foodie.identity.address.entity.Address;
import com.example.foodie.identity.address.service.AddressService;
import com.example.foodie.identity.user.entity.User;
import com.example.foodie.identity.user.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/admin/users")
@AllArgsConstructor
public class AdminUserController implements AdminUserControllerDocs {

    private final UserService userService;
    private final AddressService addressService;

    @Override
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @Override
    @PostMapping("/blocking/{id}/{type}")
    public ResponseEntity<String> blocking(@PathVariable Integer id, @PathVariable Integer type){
        userService.blocking(id, type);

        return ResponseEntity.ok("Success");
    }

    @Override
    @GetMapping("/{id}/addresses")
    public ResponseEntity<List<Address>> getAddressesByUserId(@PathVariable Integer id) {
        return ResponseEntity.ok(addressService.getAllAddressesByUserId(id));
    }
}
