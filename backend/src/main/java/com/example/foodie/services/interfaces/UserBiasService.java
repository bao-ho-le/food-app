package com.example.foodie.services.interfaces;

import org.springframework.security.core.Authentication;

import java.util.Map;

public interface UserBiasService {
    Map<String, Double> decayBiasIfSessionEnded(Authentication authentication);

    Map<String, Double> readBiasVector(String json) ;

    String writeBiasVector(Map<String, Double> biasMap) ;

}
