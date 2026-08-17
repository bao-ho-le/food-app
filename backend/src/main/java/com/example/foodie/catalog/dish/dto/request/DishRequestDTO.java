package com.example.foodie.catalog.dish.dto.request;
import com.example.foodie.catalog.tag.entity.Tag;
import com.example.foodie.catalog.restaurant.entity.Restaurant;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import java.util.List;
@Data
@Builder
public class DishRequestDTO {

    @NotNull(message = "Tên món ăn không được để trống")
    private String name;

    @NotNull(message = "Giá món ăn không được để trống")
    private float price;

    @NotNull(message = "Nhà hàng không được để trống")
    // private Restaurant restaurant;
    private Integer restaurantId;

    private List<Integer> tags; // danh sách tag của món

    private String imageUrl;
}
