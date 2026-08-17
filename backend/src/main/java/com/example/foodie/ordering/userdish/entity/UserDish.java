package com.example.foodie.ordering.userdish.entity;

import com.example.foodie.identity.user.entity.User;
import com.example.foodie.catalog.dish.entity.Dish;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Builder
// Cart
public class UserDish  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Builder.Default
    @NotNull
    private Integer quantity = 1;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name="dish_id")
    private Dish dish;
}
