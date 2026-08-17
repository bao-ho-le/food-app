package com.example.foodie.catalog.dishtag.entity;

import com.example.foodie.catalog.tag.entity.Tag;
import com.example.foodie.catalog.dish.entity.Dish;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class DishTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name="dish_id")
    private Dish dish;

    @ManyToOne
    @JoinColumn(name="tag_id")
    private Tag tag;
}
