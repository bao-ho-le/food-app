package com.example.foodie.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class OrderDish {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    @NotNull
    private Order order;

    @ManyToOne
    @JoinColumn(name = "dish_id")
    @NotNull
    private Dish dish;

    @NotNull
    private Integer quantity;

    @NotNull
    private Float price;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "review_id")
    @JsonIgnore
    private Review review;
}
