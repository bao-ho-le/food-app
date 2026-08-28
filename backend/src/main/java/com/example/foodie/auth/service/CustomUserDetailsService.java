package com.example.foodie.auth.service;

import com.example.foodie.auth.security.CustomUserDetails;
import com.example.foodie.identity.user.entity.User;
import com.example.foodie.identity.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không có user này"));

        return new CustomUserDetails(user);
    }

    public UserDetails loadUserById(Integer userId) {
        User user = userRepository.findByIdWithRole(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Không có user này"));
        return new CustomUserDetails(user);
    }
}
