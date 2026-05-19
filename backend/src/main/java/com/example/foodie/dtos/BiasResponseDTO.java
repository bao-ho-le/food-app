package com.example.foodie.dtos;

import com.example.foodie.models.Tag;
import com.example.foodie.models.UserBias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BiasResponseDTO {
    private Float score;
    private Tag tag;

    public static BiasResponseDTO from(UserBias userBias) {
        return BiasResponseDTO.builder()
                .score(userBias.getScore())
                .tag(userBias.getBias().getTag())
                .build();
    }
}
