package com.example.foodie.identity.admin.user.controller;

import com.example.foodie.identity.address.entity.Address;
import com.example.foodie.identity.user.entity.User;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

import static com.example.foodie.common.config.OpenApiConfig.BEARER_SECURITY_SCHEME;

@Tag(name = "Admin - User", description = "Quản trị người dùng (chỉ ADMIN)")
@SecurityRequirement(name = BEARER_SECURITY_SCHEME)
public interface AdminUserControllerDocs {

    ResponseEntity<List<User>> getAllUsers();

    ResponseEntity<String> blocking(@PathVariable Integer id, @PathVariable Integer type);

    ResponseEntity<List<Address>> getAddressesByUserId(@PathVariable Integer id);
}
