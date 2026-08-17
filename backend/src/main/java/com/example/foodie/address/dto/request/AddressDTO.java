package com.example.foodie.address.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Địa chỉ giao hàng của người dùng")
public class AddressDTO {

    @NotNull
    private String address;

    @Builder.Default
    @NotNull
    private Boolean isDefault = false;
}
