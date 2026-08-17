package com.example.foodie.ordering.order.entity;

import com.example.foodie.feedback.review.entity.Review;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.example.foodie.catalog.dish.entity.Dish;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class OrderDish {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id",
            foreignKey = @ForeignKey(foreignKeyDefinition = "FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE"))
    @NotNull
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dish_id",
            foreignKey = @ForeignKey(foreignKeyDefinition = "FOREIGN KEY (dish_id) REFERENCES dish (id) ON DELETE RESTRICT"))
    @NotNull
    private Dish dish;

    @NotNull
    private Integer quantity;

    @NotNull
    private Float price;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id",
            foreignKey = @ForeignKey(foreignKeyDefinition = "FOREIGN KEY (review_id) REFERENCES review (id) ON DELETE SET NULL"))
    @JsonIgnore
    private Review review;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
