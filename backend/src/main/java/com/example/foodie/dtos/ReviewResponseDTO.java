package com.example.foodie.dtos;

import java.time.LocalDateTime;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor 
@NoArgsConstructor
public class ReviewResponseDTO {
    @NotNull
    private String userName;

    private String comment;

    @NotNull
    private Integer rating;

    @NotNull
    private LocalDateTime createdAt;

}
