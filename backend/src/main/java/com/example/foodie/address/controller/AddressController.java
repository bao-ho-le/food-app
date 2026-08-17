package com.example.foodie.address.controller;

import com.example.foodie.address.dto.request.AddressDTO;
import com.example.foodie.common.base.BaseController;
import com.example.foodie.address.entity.Address;
import com.example.foodie.address.service.AddressService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.foodie.common.config.OpenApiConfig.BEARER_SECURITY_SCHEME;


@RestController
@RequestMapping("${api.prefix}/address")
@Tag(name = "Address", description = "Quản lý địa chỉ giao hàng của người dùng")
@SecurityRequirement(name = BEARER_SECURITY_SCHEME)
public class AddressController extends BaseController<Address> {
    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        super(addressService);
        this.addressService = addressService;
    }

    @Operation(summary = "Thêm địa chỉ", description = "Thêm địa chỉ giao hàng mới cho người dùng đang đăng nhập.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Thêm địa chỉ thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ")
    })
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

    @Operation(summary = "Lấy danh sách địa chỉ", description = "Trả về toàn bộ địa chỉ giao hàng của người dùng đang đăng nhập.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
    })
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

    @Operation(summary = "Xoá địa chỉ", description = "Xoá một địa chỉ giao hàng theo ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Xoá địa chỉ thành công")
    })
    @DeleteMapping("/user/{address_id}")
    public ResponseEntity<?> deleteAddress(
            @Parameter(description = "ID của địa chỉ") @PathVariable(name="address_id") Integer addressId) {
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

    @Operation(summary = "Cập nhật địa chỉ", description = "Cập nhật thông tin một địa chỉ giao hàng theo ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ hoặc địa chỉ không tồn tại")
    })
    @PutMapping("/user/{address_id}")
    public ResponseEntity<?> updateAddress(Authentication authentication,
                                            @Parameter(description = "ID của địa chỉ") @PathVariable(name="address_id") Integer addressId,
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
