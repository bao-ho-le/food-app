package com.example.foodie.identity.user.service;

import com.example.foodie.common.exception.ErrorCode;
import com.example.foodie.common.exception.business_exception.IdentityException;
import com.example.foodie.security.CustomUserDetails;
import com.example.foodie.security.JWTService;
import com.example.foodie.identity.user.dto.request.AdminDTO;
import com.example.foodie.identity.user.dto.request.ResetPasswordDTO;
import com.example.foodie.identity.user.dto.request.UserDTO;
import com.example.foodie.identity.user.dto.request.UserLoginDTO;
import com.example.foodie.identity.user.dto.request.UserProfileUpdateDTO;
import com.example.foodie.identity.user.dto.response.AdminResponseDTO;
import com.example.foodie.identity.user.dto.response.UserLoginResponseDTO;
import com.example.foodie.identity.user.dto.response.UserProfileDTO;
import com.example.foodie.identity.user.dto.response.UserResponseDTO;
import com.example.foodie.identity.user.entity.Role;
import com.example.foodie.identity.user.entity.User;
import com.example.foodie.identity.user.enums.RoleName;
import com.example.foodie.identity.user.helper.UserHelper;
import com.example.foodie.identity.user.mapper.UserMapper;
import com.example.foodie.identity.user.repository.RoleRepository;
import com.example.foodie.identity.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JWTService jwtService;
    private final UserHelper userHelper;
    private final UserMapper userMapper;

    @Override
    public UserResponseDTO register(UserDTO userDTO){
        userHelper.validateUserRequest(userDTO);

        if(userRepository.existsByEmail(userDTO.getEmail())){
            throw new IdentityException(ErrorCode.USER_EMAIL_ALREADY_EXISTS);
        }
        else if(existsByPhoneNumber(userDTO.getPhoneNumber())){
            throw new IdentityException(ErrorCode.USER_PHONE_ALREADY_EXISTS);
        }

        Role role = roleRepository.findByRoleName(RoleName.USER)
                .orElseThrow(() -> new IdentityException(ErrorCode.USER_ROLE_NOT_FOUND));

        String encodedPassword = encoder.encode(userDTO.getPassword());
        userDTO.setPassword(encodedPassword);

        String token = jwtService.generateToken(userDTO.getEmail());

        userRepository.save(userMapper.toEntity(userDTO, role));

        return userMapper.toRegisterResponse(userDTO, token);
    }

    @Override
    public AdminResponseDTO registerAdmin(AdminDTO adminDTO){
        userHelper.validateUserRequest(adminDTO);

        if (userRepository.existsByEmail(adminDTO.getEmail())){
            throw new IdentityException(ErrorCode.USER_EMAIL_ALREADY_EXISTS);
        }
        else if (existsByPhoneNumber(adminDTO.getPhoneNumber())) {
            throw new IdentityException(ErrorCode.USER_PHONE_ALREADY_EXISTS);
        }

        Role role = roleRepository.findByRoleName(RoleName.ADMIN)
                .orElseThrow(() -> new IdentityException(ErrorCode.USER_ROLE_NOT_FOUND));

        String encodedPassword = encoder.encode(adminDTO.getPassword());
        adminDTO.setPassword(encodedPassword);
        String token = jwtService.generateToken(adminDTO.getEmail());

        userRepository.save(userMapper.toEntity(adminDTO, role));

        return userMapper.toAdminRegisterResponse(adminDTO, token);
    }

    @Override
    public UserLoginResponseDTO login(UserLoginDTO userLoginDTO) {
        userHelper.validateLoginRequest(userLoginDTO);

        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(userLoginDTO.getEmail(), userLoginDTO.getPassword()));

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        String token = jwtService.generateToken(userLoginDTO.getEmail());

        return userMapper.toLoginResponse(user, userLoginDTO.getEmail(), token);
    }

    @Override
    public void resetPassword(ResetPasswordDTO resetPasswordDTO){
        userHelper.validateResetPasswordRequest(resetPasswordDTO);

        User user = userRepository.findByEmail(resetPasswordDTO.getEmail())
                .orElseThrow(() -> new IdentityException(ErrorCode.USER_NOT_FOUND));

        if (encoder.matches(resetPasswordDTO.getOldPassword(), user.getPassword())){
            String encodedPassword = encoder.encode(resetPasswordDTO.getNewPassword());
            user.setPassword(encodedPassword);
            userRepository.save(user);
        }
        else {
            throw new IdentityException(ErrorCode.USER_OLD_PASSWORD_INCORRECT);
        }
    }

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
    public UserResponseDTO getUserByEmail(String email) {
        userHelper.validateEmail(email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IdentityException(ErrorCode.USER_NOT_FOUND));

        return userMapper.toUserResponse(user);
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

        user.setActive(type == 1);

        userRepository.save(user);
    }
}
