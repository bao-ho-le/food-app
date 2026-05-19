//package com.example.foodie.services.impl;
//
//import com.example.foodie.models.User;
//import com.example.foodie.repos.UserRepository;
//import com.example.foodie.services.interfaces.UserBiasService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import jakarta.transaction.Transactional;
//import lombok.AllArgsConstructor;
//import org.springframework.security.core.Authentication;
//import org.springframework.stereotype.Service;
//
//import java.time.Duration;
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Optional;
//
//@Service
//@AllArgsConstructor
//public class UserBiasServiceImpl extends UserBiasService {
//    private final ObjectMapper objectMapper;
//    private final UserRepository userRepository;
//
//    @Override
//    @Transactional
//    public Map<String, Double> decayBiasIfSessionEnded(Authentication authentication) {
//        String email = authentication.getName();
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại"));
//
//        LocalDateTime now = LocalDateTime.now();
//        LocalDateTime lastInteraction = Optional.ofNullable(user.getLastInteractionAt())
//                .orElse(user.getCreatedAt());
//
//        Map<String, Double> biasMap = readBiasVector(user.getBiasVectorJson());
//
//        // Nếu chưa hết phiên → không làm gì
//        if (lastInteraction != null
//                && Duration.between(lastInteraction, now).compareTo(SESSION_TIMEOUT) < 0) {
//            return biasMap;
//        }
//
//        // Hết phiên → áp dụng decay
//        Map<String, Double> decayed = new HashMap<>(biasMap.size());
//        for (Map.Entry<String, Double> entry : biasMap.entrySet()) {
//            double decayedValue = Math.max(0.0d, entry.getValue() * DECAY_RATE);
//            decayed.put(entry.getKey(), decayedValue);
//        }
//
//        user.setBiasVectorJson(writeBiasVector(decayed));
//        user.setLastInteractionAt(now);
//        userRepository.save(user);
//
//        return decayed;
//    }
//
//    private Map<String, Double> readBiasVector(String json) {
//        if (json == null || json.isBlank()) {
//            return new HashMap<>();
//        }
//        try {
//            return objectMapper.readValue(json, new TypeReference<Map<String, Double>>() {});
//        } catch (IOException ex) {
//            throw new IllegalStateException("Không thể parse bias vector JSON", ex);
//        }
//    }
//
//    private String writeBiasVector(Map<String, Double> biasMap) {
//        try {
//            return objectMapper.writeValueAsString(biasMap);
//        } catch (JsonProcessingException ex) {
//            throw new IllegalStateException("Không thể serialize bias vector JSON", ex);
//        }
//    }
//}
