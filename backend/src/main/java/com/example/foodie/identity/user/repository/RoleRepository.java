package com.example.foodie.identity.user.repository;

import com.example.foodie.identity.user.enums.RoleName;
import com.example.foodie.identity.user.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByRoleName(RoleName roleName);
}
