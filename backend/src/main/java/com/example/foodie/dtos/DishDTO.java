package com.example.foodie.dtos;

import com.example.foodie.models.Tag;
import com.example.foodie.models.Restaurant;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DishDTO {

    private Integer id;

    @NotNull(message = "Tên món ăn không được để trống")
    private String name;

    @NotNull(message = "Giá món ăn không được để trống")
    private float price;

    @NotNull(message = "Nhà hàng không được để trống")
    private Restaurant restaurant;

    private boolean isAvailable = true;

    private float rating; // điểm đánh giá món ăn

    private List<Tag> tags; // danh sách tag của món

    private String url;
}
