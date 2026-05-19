package com.example.foodie.config;

import com.example.foodie.enums.RoleName;
import com.example.foodie.models.Role;
import com.example.foodie.repos.RoleRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/* Dùng CommandLineRunner và run() để khởi chạy một đoạn code ngay khi Spring khởi động
   Ở đây là tạo role Admin và User vào database
 */

@Component
@AllArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        if(roleRepository.findByRoleName(RoleName.USER).isEmpty()) {
            roleRepository.save(new Role(null, RoleName.USER));
        }
        if(roleRepository.findByRoleName(RoleName.ADMIN).isEmpty()) {
            roleRepository.save(new Role(null, RoleName.ADMIN));
        }
    }
}
