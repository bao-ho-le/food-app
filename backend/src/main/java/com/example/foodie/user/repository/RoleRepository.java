package com.example.foodie.user.repository;

import com.example.foodie.user.enums.RoleName;
import com.example.foodie.user.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByRoleName(RoleName roleName);
}
