package com.example.foodie.services.impl;
import com.example.foodie.dtos.*;
import com.example.foodie.enums.RoleName;
import com.example.foodie.models.Role;
import com.example.foodie.models.User;
import com.example.foodie.repos.RoleRepository;
import com.example.foodie.repos.UserRepository;
import com.example.foodie.security.CustomUserDetails;
import com.example.foodie.security.JWTService;
import com.example.foodie.services.interfaces.BiasService;
import com.example.foodie.services.interfaces.UserService;
import lombok.AllArgsConstructor;

import java.util.List;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder encoder;
    private AuthenticationManager authManager;
    private JWTService jwtService;
    private final BiasService biasService;

    @Override
    public UserResponseDTO register(UserDTO userDTO){
        if(userRepository.existsByEmail(userDTO.getEmail())){
            throw new RuntimeException("Email đã tồn tại");
        }
        else if(existsByPhoneNumber(userDTO.getPhoneNumber())){
            throw new RuntimeException("Sdt đã tồn tại");
        }

        Role role = roleRepository.findByRoleName(RoleName.USER)
                .orElseThrow(() -> new RuntimeException("Role USER không tồn tại trong database"));

        String encodedPassword = encoder.encode(userDTO.getPassword());
        userDTO.setPassword(encodedPassword);

        String token = jwtService.generateToken(userDTO.getEmail());

        User user = User.createUserFromDTO(userDTO, role);

        userRepository.save(user);
        biasService.attachAllBiasesToUser(userDTO.getEmail());

        return UserResponseDTO.createUserResponseFromDTO(userDTO, token);
    }

    @Override
    public AdminResponseDTO registerAdmin(AdminDTO adminDTO){
        if (userRepository.existsByEmail(adminDTO.getEmail())){
            throw new RuntimeException("Email đã tồn tại");
        }
        else if (existsByPhoneNumber(adminDTO.getPhoneNumber())) {
            throw new RuntimeException("Sdt đã tồn tại");
        }

        Role role = roleRepository.findByRoleName(RoleName.ADMIN)
                .orElseThrow(() -> new RuntimeException("Role ADMIN không tồn tại"));

        String encodedPassword = encoder.encode(adminDTO.getPassword());
        adminDTO.setPassword(encodedPassword);
        String token = jwtService.generateToken(adminDTO.getEmail());


        User admin = User.createUserFromDTO(adminDTO, role);
        userRepository.save(admin);

        return AdminResponseDTO.createAdminResponseFromDTO(adminDTO, token);
    }

//    @Override
//    public UserLoginResponseDTO login(UserLoginDTO userLoginDTO){
//        User user = userRepository.findByEmail(userLoginDTO.getEmail())
//                .orElseThrow(() -> new RuntimeException("Email hoặc password không đúng"));
//
//        if (encoder.matches(userLoginDTO.getPassword(), user.getPassword())){
//            return UserLoginResponseDTO.builder()
//                    .email(user.getEmail())
//                    .roleName(user.getRole().getRoleName())
//                    .build();
//        }
//        else {
//            throw new RuntimeException("Email hoặc password không đúng");
//        }
//    }

    @Override
    public UserLoginResponseDTO login(UserLoginDTO userLoginDTO) {
        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(userLoginDTO.getEmail(), userLoginDTO.getPassword()));

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        String token = jwtService.generateToken(userLoginDTO.getEmail());

        return UserLoginResponseDTO.builder()
                .email(userLoginDTO.getEmail())
                .roleName(user.getRole().getRoleName())
                .token(token)
                .build();
    }

    @Override
    public void resetPassword(ResetPasswordDTO resetPasswordDTO){
        User user = userRepository.findByEmail(resetPasswordDTO.getEmail())
                .orElseThrow(() -> new RuntimeException("Email không đúng"));

        if (encoder.matches(resetPasswordDTO.getOldPassword(), user.getPassword())){
            String encodedPassword = encoder.encode(resetPasswordDTO.getNewPassword());
            user.setPassword(encodedPassword);
            userRepository.save(user);
        }
        else {
            throw new RuntimeException("Password không đúng");
        }
    }

    @Override
    public UserProfileDTO getUserProfileByToken(Authentication authentication){

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        return UserProfileDTO.builder()
                .email(user.getEmail())
                .gender(user.getGender())
                .phoneNumber(user.getPhoneNumber())
                .birthday(user.getBirthday())
                .fullName(user.getFullName())
                .build();
    }

    @Override
    public UserProfileDTO updateProfile(Authentication authentication, UserProfileUpdateDTO userProfileUpdateDTO){
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        user.setFullName(userProfileUpdateDTO.getFullName());
        user.setEmail(userProfileUpdateDTO.getEmail());
        user.setPhoneNumber(userProfileUpdateDTO.getPhoneNumber());
        user.setGender(userProfileUpdateDTO.getGender());
        user.setBirthday(userProfileUpdateDTO.getBirthday());

        userRepository.save(user);


        return UserProfileDTO.builder()
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .gender(user.getGender())
                .birthday(user.getBirthday())
                .build();
    }



     /* @PostConstruct là annotation của Java, nó đánh dấu một phương thức sẽ tự động chạy một lần duy
        nhất ngay sau khi bean được khởi tạo
        Dùng ở đây để mã hoá tất cả mật khẩu chưa được mã hoá, do chức năng Security ở project này được thêm vào sau cùng
     */

//    @PostConstruct
//    public void encodeAllPasswords() {
//        List<User> users = userRepository.findAll();
//        for (User user : users) {
//            if (!user.getPassword().startsWith("$2a$")) { // tránh mã hóa lại
//                user.setPassword(encoder.encode(user.getPassword()));
//                System.out.println("Đã mã hóa cho user: " + user.getEmail());
//            }
//        }
//        userRepository.saveAll(users);
//        System.out.println("✅ Đã mã hóa xong toàn bộ mật khẩu trong DB!");
//    }


    /* Dưới này là những hàm của lớp Repository
       Controller không nên gọi trực tiếp Repo, nhưng phải sử dụng các hàm bên dưới, vì vậy phải khai
       báo ở lớp Service để Controller có thể gọi
    */

    @Override
    public UserResponseDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        return UserResponseDTO.builder()
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .gender(user.getGender())
                .birthday(user.getBirthday())
                .build();
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByPhoneNumber(String phoneNumber) {
        return userRepository.existsByPhoneNumber(phoneNumber);
    }

    @Override
    public List<User> getAllUsers(Authentication authentication) {
        String email = authentication.getName();
        User requestingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        if (requestingUser.getRole().getRoleName() != RoleName.ADMIN) {
            throw new RuntimeException("Chỉ admin mới có quyền truy cập danh sách người dùng");
        }

        return userRepository.findAll();
    }
    
    @Override
    public void blocking(Integer id, Integer type){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        if(type == 0){
            user.setActive(false);
        } else if(type == 1){
            user.setActive(true);
        } else {
            throw new RuntimeException("Loại không hợp lệ");
        }
        userRepository.save(user);
        }


}