package com.example.foodie.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddressDTO {

    @NotNull
    private String address;

    @Builder.Default
    @NotNull
    private Boolean isDefault = false;
}
