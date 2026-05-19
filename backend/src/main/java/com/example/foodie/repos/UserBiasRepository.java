package com.example.foodie.repos;

import com.example.foodie.models.UserBias;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserBiasRepository extends JpaRepository<UserBias, Integer> {
    Optional<UserBias> findByUser_IdAndBias_Tag_Id(int userId, int tagId);
    boolean existsByUser_IdAndBias_Tag_Id(int userId, int tagId);
    List<UserBias> findAllByUser_Id(int userId);
}
