package com.example.foodie.feedback.review.dto.response;

import java.time.LocalDateTime;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Đánh giá của một người dùng cho một món ăn")
public class ReviewResponseDTO {
    @NotNull
    private String userName;

    private String comment;

    @NotNull
    private Integer rating;

    @NotNull
    private LocalDateTime createdAt;

}
