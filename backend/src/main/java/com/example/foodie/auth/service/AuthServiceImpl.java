package com.example.foodie.auth.service;

import com.example.foodie.common.exception.ErrorCode;
import com.example.foodie.common.exception.business_exception.IdentityException;
import com.example.foodie.auth.dto.request.AdminDTO;
import com.example.foodie.auth.dto.request.ResetPasswordDTO;
import com.example.foodie.auth.dto.request.UserDTO;
import com.example.foodie.auth.dto.request.UserLoginDTO;
import com.example.foodie.auth.dto.response.AdminResponseDTO;
import com.example.foodie.auth.dto.response.UserLoginResponseDTO;
import com.example.foodie.auth.dto.response.UserResponseDTO;
import com.example.foodie.auth.helper.AuthHelper;
import com.example.foodie.auth.mapper.AuthMapper;
import com.example.foodie.identity.user.entity.Role;
import com.example.foodie.identity.user.entity.User;
import com.example.foodie.identity.user.enums.RoleName;
import com.example.foodie.identity.user.repository.RoleRepository;
import com.example.foodie.identity.user.repository.UserRepository;
import com.example.foodie.auth.security.CustomUserDetails;
import com.example.foodie.auth.security.JWTService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JWTService jwtService;
    private final AuthHelper authHelper;
    private final AuthMapper authMapper;

    @Override
    public UserResponseDTO register(UserDTO userDTO) {
        authHelper.validateUserRequest(userDTO);

        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new IdentityException(ErrorCode.USER_EMAIL_ALREADY_EXISTS);
        } else if (userRepository.existsByPhoneNumber(userDTO.getPhoneNumber())) {
            throw new IdentityException(ErrorCode.USER_PHONE_ALREADY_EXISTS);
        }

        Role role = roleRepository.findByRoleName(RoleName.USER)
                .orElseThrow(() -> new IdentityException(ErrorCode.USER_ROLE_NOT_FOUND));

        String encodedPassword = encoder.encode(userDTO.getPassword());
        userDTO.setPassword(encodedPassword);

        String token = jwtService.generateToken(userDTO.getEmail());

        userRepository.save(authMapper.toEntity(userDTO, role));

        return authMapper.toRegisterResponse(userDTO, token);
    }

    @Override
    public AdminResponseDTO registerAdmin(AdminDTO adminDTO) {
        authHelper.validateUserRequest(adminDTO);

        if (userRepository.existsByEmail(adminDTO.getEmail())) {
            throw new IdentityException(ErrorCode.USER_EMAIL_ALREADY_EXISTS);
        } else if (userRepository.existsByPhoneNumber(adminDTO.getPhoneNumber())) {
            throw new IdentityException(ErrorCode.USER_PHONE_ALREADY_EXISTS);
        }

        Role role = roleRepository.findByRoleName(RoleName.ADMIN)
                .orElseThrow(() -> new IdentityException(ErrorCode.USER_ROLE_NOT_FOUND));

        String encodedPassword = encoder.encode(adminDTO.getPassword());
        adminDTO.setPassword(encodedPassword);
        String token = jwtService.generateToken(adminDTO.getEmail());

        userRepository.save(authMapper.toEntity(adminDTO, role));

        return authMapper.toAdminRegisterResponse(adminDTO, token);
    }

    @Override
    public UserLoginResponseDTO login(UserLoginDTO userLoginDTO) {
        authHelper.validateLoginRequest(userLoginDTO);

        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(userLoginDTO.getEmail(), userLoginDTO.getPassword()));

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        String token = jwtService.generateToken(userLoginDTO.getEmail());

        return authMapper.toLoginResponse(user, userLoginDTO.getEmail(), token);
    }

    @Override
    public void resetPassword(ResetPasswordDTO resetPasswordDTO) {
        authHelper.validateResetPasswordRequest(resetPasswordDTO);

        User user = userRepository.findByEmail(resetPasswordDTO.getEmail())
                .orElseThrow(() -> new IdentityException(ErrorCode.USER_NOT_FOUND));

        if (encoder.matches(resetPasswordDTO.getOldPassword(), user.getPassword())) {
            String encodedPassword = encoder.encode(resetPasswordDTO.getNewPassword());
            user.setPassword(encodedPassword);
            userRepository.save(user);
        } else {
            throw new IdentityException(ErrorCode.USER_OLD_PASSWORD_INCORRECT);
        }
    }
}
