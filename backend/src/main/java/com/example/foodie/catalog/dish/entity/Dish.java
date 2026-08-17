package com.example.foodie.catalog.dish.entity;

import com.example.foodie.catalog.restaurant.entity.Restaurant;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Dish {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    private String name;

    @NotNull
    private float price;

    @ManyToOne
    @JoinColumn(name="restaurant_id", referencedColumnName="id")
    private Restaurant restaurant;

    @Builder.Default
    private boolean isAvailable = true;

}
