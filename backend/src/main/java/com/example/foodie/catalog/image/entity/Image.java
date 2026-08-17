package com.example.foodie.catalog.image.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.example.foodie.catalog.dish.entity.Dish;
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
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // @NotNull
    private String imageName;

    @Builder.Default
    private Boolean isThumbnail = false;

    @NotNull
    private String url;

    @ManyToOne
    @JoinColumn(name="image_id", referencedColumnName="id")
    private Dish dish;

    private String publicId;

    private String format;

    private Long size;

    private Integer width;

    private Integer height;

    private String altText;
}
