package com.example.foodie.identity.user.service;

import com.example.foodie.auth.repository.RefreshTokenRepository;
import com.example.foodie.common.exception.ErrorCode;
import com.example.foodie.common.exception.business_exception.IdentityException;
import com.example.foodie.identity.user.dto.request.UserProfileUpdateDTO;
import com.example.foodie.identity.user.dto.response.UserProfileDTO;
import com.example.foodie.identity.user.entity.User;
import com.example.foodie.identity.user.helper.UserHelper;
import com.example.foodie.identity.user.mapper.UserMapper;
import com.example.foodie.identity.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserHelper userHelper;
    private final UserMapper userMapper;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public UserProfileDTO getUserProfileByToken(Authentication authentication){
        User user = userHelper.getUserFromAuthentication(authentication);

        return userMapper.toProfile(user);
    }

    @Override
    public UserProfileDTO updateProfile(Authentication authentication, UserProfileUpdateDTO userProfileUpdateDTO){
        userHelper.validateProfileUpdateRequest(userProfileUpdateDTO);

        User user = userHelper.getUserFromAuthentication(authentication);

        user.setFullName(userProfileUpdateDTO.getFullName());
        user.setEmail(userProfileUpdateDTO.getEmail());
        user.setPhoneNumber(userProfileUpdateDTO.getPhoneNumber());
        user.setGender(userProfileUpdateDTO.getGender());
        user.setBirthday(userProfileUpdateDTO.getBirthday());

        userRepository.save(user);

        return userMapper.toProfile(user);
    }

    /* Dưới này là những hàm của lớp Repository
       Controller không nên gọi trực tiếp Repo, nhưng phải sử dụng các hàm bên dưới, vì vậy phải khai
       báo ở lớp Service để Controller có thể gọi
    */

    @Override
    public UserProfileDTO getUserByEmail(String email) {
        userHelper.validateEmail(email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IdentityException(ErrorCode.USER_NOT_FOUND));

        return userMapper.toProfile(user);
    }

    @Override
    public boolean existsByEmail(String email) {
        userHelper.validateEmail(email);

        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByPhoneNumber(String phoneNumber) {
        userHelper.validatePhoneNumber(phoneNumber);

        return userRepository.existsByPhoneNumber(phoneNumber);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public void blocking(Integer id, Integer type){
        userHelper.validateUserId(id);
        userHelper.validateBlockingType(type);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IdentityException(ErrorCode.USER_NOT_FOUND));

        boolean active = (type == 1);
        user.setActive(active);
        userRepository.save(user);

        if (!active) {
            refreshTokenRepository.revokeAllByUser(user, Instant.now());
        }
    }
}
