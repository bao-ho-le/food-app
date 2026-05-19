package com.example.foodie.services.interfaces;

import com.example.foodie.dtos.BiasDTO;
import com.example.foodie.dtos.BiasResponseDTO;
import com.example.foodie.models.UserBias;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface BiasService {
    UserBias addBias(Authentication authentication, BiasDTO biasDTO);
    UserBias updateBias(Authentication authentication, BiasDTO biasDTO);
    List<BiasResponseDTO> getAllBiasByUser(Authentication authentication);
    void attachAllBiasesToUser(String email);
}
