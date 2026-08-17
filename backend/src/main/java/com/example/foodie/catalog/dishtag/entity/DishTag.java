package com.example.foodie.catalog.dishtag.entity;

import com.example.foodie.catalog.tag.entity.Tag;
import com.example.foodie.catalog.dish.entity.Dish;
import jakarta.persistence.*;
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
public class DishTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="dish_id",
            foreignKey = @ForeignKey(foreignKeyDefinition = "FOREIGN KEY (dish_id) REFERENCES dish (id) ON DELETE CASCADE"))
    private Dish dish;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="tag_id",
            foreignKey = @ForeignKey(foreignKeyDefinition = "FOREIGN KEY (tag_id) REFERENCES tag (id) ON DELETE CASCADE"))
    private Tag tag;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
