package com.example.foodie.services.impl;

import com.example.foodie.dtos.BiasDTO;
import com.example.foodie.dtos.BiasResponseDTO;
import com.example.foodie.models.Bias;
import com.example.foodie.models.Tag;
import com.example.foodie.models.User;
import com.example.foodie.models.UserBias;
import com.example.foodie.repos.BiasRepository;
import com.example.foodie.repos.TagRepository;
import com.example.foodie.repos.UserBiasRepository;
import com.example.foodie.repos.UserRepository;
import com.example.foodie.services.interfaces.BiasService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class BiasServiceImpl implements BiasService {
        private BiasRepository biasRepository;
        private UserRepository userRepository;
        private TagRepository tagRepository;
        private UserBiasRepository userBiasRepository;

    @Override
        public UserBias addBias(Authentication authentication, BiasDTO biasDTO){

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        Tag tag = tagRepository.findById(biasDTO.getTagId())
                .orElseThrow(() -> new RuntimeException("Tag không tồn tại"));

        Optional<UserBias> biasExisting = userBiasRepository.findByUser_IdAndBias_Tag_Id(
            user.getId(),
            biasDTO.getTagId()
        );

        if (biasExisting.isPresent()){
            throw new RuntimeException("Tag này đã có rồi.");
        }

        Bias bias = biasRepository.findByTag_Id(tag.getId())
            .orElseGet(() -> biasRepository.save(Bias.builder()
                .tag(tag)
                .build()));

        UserBias newBias = UserBias.builder()
            .user(user)
            .bias(bias)
            .score(resolveScore(biasDTO.getScore()))
            .build();

        return userBiasRepository.save(newBias);
    }

    @Override
        public UserBias updateBias(Authentication authentication, BiasDTO biasDTO){

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        Tag tag = tagRepository.findById(biasDTO.getTagId())
                .orElseThrow(() -> new RuntimeException("Tag không tồn tại"));

        Optional<UserBias> biasExisting = userBiasRepository.findByUser_IdAndBias_Tag_Id(
            user.getId(),
            biasDTO.getTagId()
        );

        if(biasExisting.isPresent()){

            UserBias updatedBias = biasExisting.get();
            updatedBias.setScore(resolveScore(biasDTO.getScore()));
            return userBiasRepository.save(updatedBias);

        } else{
            Bias bias = biasRepository.findByTag_Id(tag.getId())
                .orElseGet(() -> biasRepository.save(Bias.builder()
                    .tag(tag)
                    .build()));

            UserBias newBias = UserBias.builder()
                .user(user)
                .bias(bias)
                .score(resolveScore(biasDTO.getScore()))
                .build();

            return userBiasRepository.save(newBias);
        }
    }

    @Override
        public List<BiasResponseDTO> getAllBiasByUser(Authentication authentication){
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        List<UserBias> userBias = userBiasRepository.findAllByUser_Id(user.getId());

        System.out.println(userBias);

        return userBias.stream()
                .map(BiasResponseDTO::from)
                .toList();
    }

    @Override
    public void attachAllBiasesToUser(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        List<Tag> allTags = tagRepository.findAll();

        if (allTags.isEmpty())
            throw new RuntimeException("Hiện chưa có tag nào trong hệ thống");

        for (Tag tag : allTags) {
            Bias bias = biasRepository.findByTag_Id(tag.getId())
                .orElseGet(() -> biasRepository.save(Bias.builder()
                    .tag(tag)
                    .build()));

            boolean alreadyHas = userBiasRepository.existsByUser_IdAndBias_Tag_Id(user.getId(), tag.getId());

            if (!alreadyHas) {
            UserBias userBias = UserBias.builder()
                .user(user)
                .bias(bias)
                .score(2.5f)
                .build();
            userBiasRepository.save(userBias);
            }
        }

    }

        private Float resolveScore(Float score) {
        return score != null ? score : 2.5f;
        }
}
