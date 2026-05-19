package com.example.foodie.services.impl;

import com.example.foodie.dtos.BiasResponseDTO;
import com.example.foodie.dtos.UpdateUserTagsDTO;
import com.example.foodie.models.*;
import com.example.foodie.repos.*;
import com.example.foodie.services.interfaces.BiasService;
import com.example.foodie.services.interfaces.DishService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

import com.example.foodie.services.interfaces.RecommendationService;

@Service
public class RecommendationServiceImpl implements RecommendationService {

    @Value("${fastapi.url}")
    private String fastApiUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final BiasService biasService;
    private final UserRepository userRepository;
    private final DishRepository dishRepository;
    private final TagRepository tagRepository;
    private final DishTagRepository dishTagRepository;
    private final BiasRepository biasRepository;
    private final UserBiasRepository userBiasRepository;

    public RecommendationServiceImpl(TagRepository tagRepository,
                                     DishService dishService,
                                     BiasService biasService,
                                     UserRepository userRepository,
                                     DishRepository dishRepository,
                                     DishTagRepository dishTagRepository,
                                     BiasRepository biasRepository,
                                     UserBiasRepository userBiasRepository) {
        this.biasService = biasService;
        this.userRepository = userRepository;
        this.dishRepository = dishRepository;
        this.tagRepository = tagRepository;
        this.dishTagRepository = dishTagRepository;
        this.biasRepository = biasRepository;
        this.userBiasRepository = userBiasRepository;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Float> getRecommendations(Authentication authentication) {

        // Lấy user
        String email = authentication.getName();

        boolean exists = userRepository.existsByEmail(email);

        if (!exists) {
            throw new RuntimeException("Email không tồn tại");
        }

        /* Lấy tất cả bias của user có dạng
        bias: {
            "Grill": 2.5,
            "Spicy": 2.5,
            ...}
         */
        List<BiasResponseDTO> allBiasOfUser = biasService.getAllBiasByUser(authentication);

        Map<String, Float> allBias = new HashMap<>();
        for(BiasResponseDTO bias: allBiasOfUser){
            allBias.put(bias.getTag().getName(), bias.getScore());
        }

        // Lấy tất cả candidates (hiện tại do hệ thống chỉ có 100 món nên lấy hết)
        // Lưu candidates dưới dạng:
        /*
        candidates: [
            {"dish_id": 1, "item_tags": ["grill", "spicy", ...]},
            {"dish_id": 2, "item_tags": ["vegan", "sweet", ...]},
            ...
        ]
         */
        List<Map<String, Object>> candidates = dishRepository.findAll().stream()
                .map(dish -> {
                    Map<String, Object> candidate = new HashMap<>();
                    candidate.put("dish_id", dish.getId());
                    candidate.put("item_tags", dishTagRepository.findTagNamesByDishId(dish.getId()));
                    return candidate;
                })
                .toList();


        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("user_bias", allBias);
        requestBody.put("candidates", candidates);
        requestBody.put("top_k", 10);

        Map<String, Float> response = (Map<String, Float>) restTemplate.postForObject(
            fastApiUrl + "/recommend",
                requestBody,
                Map.class
        );

        System.out.println(response);
        return response;

    }


    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void updateUserTags(Authentication authentication, UpdateUserTagsDTO dto){
        // Lấy user
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        /* Lấy tất cả bias của user có dạng
        bias: {
            "Grill": 2.5,
            "Spicy": 2.5,
            ...}
         */
        List<BiasResponseDTO> allBiasOfUser = biasService.getAllBiasByUser(authentication);

        Map<String, Float> allBias = new HashMap<>();
        for(BiasResponseDTO bias: allBiasOfUser){
            allBias.put(bias.getTag().getName(), bias.getScore());
        }

        // Lấy tất cả tags của món ăn, id của món nằm trong dto
        // "dish_tags": ["spicy", "noodle",...]
        Dish dish = dishRepository.findById(dto.getDishId())
                .orElseThrow(() -> new RuntimeException("Dish không tồn tại"));

        List<String> dishTagNames = dishTagRepository.findTagNamesByDishId(dish.getId());

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("user_bias", allBias);
        requestBody.put("dish_tags", dishTagNames);
        requestBody.put("action", dto.getAction());

        System.out.println(requestBody);

        ResponseEntity<Map> responseEntity = restTemplate.exchange(
            fastApiUrl + "/push_replay_buffer",
                HttpMethod.PUT,
                new HttpEntity<>(requestBody),
                Map.class
        );

        Map<String, Object> updatedBias = responseEntity.getBody();
        System.out.println("Updated bias from FastAPI: " + updatedBias);

        if (updatedBias != null && !updatedBias.isEmpty()) {
            updatedBias.forEach((tagName, scoreObj) -> {
                Float score = scoreObj instanceof Number
                        ? ((Number) scoreObj).floatValue()
                        : Float.parseFloat(scoreObj.toString());
                Tag tag = tagRepository.findByName(tagName)
                        .orElseThrow(() -> new RuntimeException("Tag không tồn tại: " + tagName));

                Bias bias = biasRepository.findByTag_Id(tag.getId())
                        .orElseGet(() -> biasRepository.save(Bias.builder().tag(tag).build()));

                UserBias userBias = userBiasRepository.findByUser_IdAndBias_Tag_Id(user.getId(), tag.getId())
                        .orElseGet(() -> UserBias.builder()
                                .user(user)
                                .bias(bias)
                                .build());

                userBias.setScore(score);
                userBiasRepository.save(userBias);
            });
        }
    }
}
