package com.example.foodie.identity.address.controller;

import com.example.foodie.identity.address.dto.request.AddressDTO;
import com.example.foodie.identity.address.entity.Address;
import com.example.foodie.identity.address.service.AddressService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.foodie.common.config.OpenApiConfig.BEARER_SECURITY_SCHEME;


@RestController
@RequestMapping("${api.prefix}/address")
@AllArgsConstructor
@Tag(name = "Address", description = "Quản lý địa chỉ giao hàng của người dùng")
@SecurityRequirement(name = BEARER_SECURITY_SCHEME)
public class AddressController {
    private final AddressService addressService;

    @GetMapping
    public ResponseEntity<List<Address>> getAll(){
        return ResponseEntity.ok(addressService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Address> getById(@PathVariable Integer id){
        return ResponseEntity.ok(addressService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Integer id){
        addressService.deleteById(id);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Thêm địa chỉ", description = "Thêm địa chỉ giao hàng mới cho người dùng đang đăng nhập.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Thêm địa chỉ thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ")
    })
    @PostMapping("/user")
    public ResponseEntity<Address> addAddressByUserId(Authentication authentication,@Valid @RequestBody AddressDTO addressDTO) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(addressService.addAddressByUserId(authentication, addressDTO));
    }

    @Operation(summary = "Lấy danh sách địa chỉ", description = "Trả về toàn bộ địa chỉ giao hàng của người dùng đang đăng nhập.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
    })
    @GetMapping("/user")
    public ResponseEntity<List<Address>> getAllAddressesByUserId(Authentication authentication) {
        return ResponseEntity.ok(addressService.getAllAddressesByUser(authentication));
    }

    @Operation(summary = "Xoá địa chỉ", description = "Xoá một địa chỉ giao hàng theo ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Xoá địa chỉ thành công")
    })
    @DeleteMapping("/user/{address_id}")
    public ResponseEntity<String> deleteAddress(
            @Parameter(description = "ID của địa chỉ") @PathVariable(name="address_id") Integer addressId) {
        addressService.deleteAddressById(addressId);

        return ResponseEntity.ok("Xoá địa chỉ thành công");
    }

    @Operation(summary = "Cập nhật địa chỉ", description = "Cập nhật thông tin một địa chỉ giao hàng theo ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ hoặc địa chỉ không tồn tại")
    })
    @PutMapping("/user/{address_id}")
    public ResponseEntity<AddressDTO> updateAddress(Authentication authentication,
                                            @Parameter(description = "ID của địa chỉ") @PathVariable(name="address_id") Integer addressId,
                                            @Valid @RequestBody AddressDTO addressDTO) {
        return ResponseEntity.ok(addressService.updateAddress(authentication, addressId, addressDTO));
    }

}
