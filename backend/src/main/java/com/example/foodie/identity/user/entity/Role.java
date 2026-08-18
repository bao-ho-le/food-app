package com.example.foodie.identity.user.entity;

import com.example.foodie.identity.user.enums.RoleName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.List;



@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message="Role không được để trống")
    @Column(unique=true)
    @Enumerated(EnumType.STRING)
    private RoleName roleName;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

}
