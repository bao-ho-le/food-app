package com.example.foodie.identity.address.controller;

import com.example.foodie.identity.address.dto.request.AddressDTO;
import com.example.foodie.identity.address.entity.Address;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

import static com.example.foodie.common.config.OpenApiConfig.BEARER_SECURITY_SCHEME;

@Tag(name = "Address", description = "Quản lý địa chỉ giao hàng của người dùng")
@SecurityRequirement(name = BEARER_SECURITY_SCHEME)
public interface AddressControllerDocs {

    @Operation(summary = "Thêm địa chỉ", description = "Thêm địa chỉ giao hàng mới cho người dùng đang đăng nhập.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Thêm địa chỉ thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ")
    })
    ResponseEntity<Address> addAddressByUserId(Authentication authentication, @Valid @RequestBody AddressDTO addressDTO);

    @Operation(summary = "Lấy danh sách địa chỉ", description = "Trả về toàn bộ địa chỉ giao hàng của người dùng đang đăng nhập.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
    })
    ResponseEntity<List<Address>> getAllAddressesByUserId(Authentication authentication);

    @Operation(summary = "Xoá địa chỉ", description = "Xoá một địa chỉ giao hàng theo ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Xoá địa chỉ thành công")
    })
    ResponseEntity<String> deleteAddress(Authentication authentication,
            @Parameter(description = "ID của địa chỉ") @PathVariable(name="address_id") Integer addressId);

    @Operation(summary = "Cập nhật địa chỉ", description = "Cập nhật thông tin một địa chỉ giao hàng theo ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ hoặc địa chỉ không tồn tại")
    })
    ResponseEntity<AddressDTO> updateAddress(Authentication authentication,
                                              @Parameter(description = "ID của địa chỉ") @PathVariable(name="address_id") Integer addressId,
                                              @Valid @RequestBody AddressDTO addressDTO);
}
