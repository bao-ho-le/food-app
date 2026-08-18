package com.example.foodie.identity.admin.config;

import com.example.foodie.identity.user.dto.request.AdminDTO;
import com.example.foodie.identity.user.enums.Gender;
import com.example.foodie.identity.user.repository.UserRepository;
import com.example.foodie.identity.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
@RequiredArgsConstructor
public class AdminAccountInitializer implements CommandLineRunner {
    private final UserService userService;
    private final UserRepository userRepository;

    @Value("${app.admin.email}") private String adminEmail;
    @Value("${app.admin.password}") private String adminPassword;
    @Value("${app.admin.full-name}") private String adminFullName;
    @Value("${app.admin.phone-number}") private String adminPhoneNumber;

    @Override
    public void run(String... args) {
        if (userRepository.existsByEmail(adminEmail)) return;

        AdminDTO adminDTO = new AdminDTO();
        adminDTO.setEmail(adminEmail);
        adminDTO.setPassword(adminPassword);
        adminDTO.setFullName(adminFullName);
        adminDTO.setPhoneNumber(adminPhoneNumber);
        adminDTO.setGender(Gender.OTHER);

        userService.registerAdmin(adminDTO);
    }
}
