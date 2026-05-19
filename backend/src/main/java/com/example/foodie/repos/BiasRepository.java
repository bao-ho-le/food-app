package com.example.foodie.repos;

import com.example.foodie.models.Bias;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BiasRepository extends JpaRepository<Bias, Integer> {
    Optional<Bias> findByTag_Id(Integer tagId);
}
