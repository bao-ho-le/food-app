package com.example.foodie.auth.controller;

import com.example.foodie.auth.dto.response.UserResponseDTO;
import com.example.foodie.auth.service.AuthService;
import com.example.foodie.support.ControllerSliceTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Bean Validation trên UserDTO (fullName/gender/phoneNumber/email/password) phải chặn ở tầng
 * MVC trước khi tới AuthService -- kiểm ở đây, không phải trong AuthServiceImplTest.
 */
@ControllerSliceTest(controllers = AuthController.class)
class AuthControllerTest {

    private static final String USERS = "/api/v1/users";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    private static Map<String, Object> validRegisterBody() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("fullName", "Nguyen Van A");
        body.put("gender", "MALE");
        body.put("phoneNumber", "0912345678");
        body.put("email", "a@example.com");
        body.put("password", "secret1");
        return body;
    }

    // ---- item 1: đăng ký hợp lệ -> 201 ----

    @Test
    void should_return201_when_registerBodyValid() throws Exception {
        when(authService.register(any(), any())).thenReturn(UserResponseDTO.builder().build());

        mockMvc.perform(post(USERS + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterBody())))
                .andExpect(status().isCreated());
    }

    // ---- item 2-5: BVA/EP trên UserDTO ----

    static Stream<Arguments> invalidRegisterBodies() {
        Map<String, Object> missingEmail = validRegisterBody();
        missingEmail.remove("email");

        Map<String, Object> malformedEmail = validRegisterBody();
        malformedEmail.put("email", "khong-phai-email");

        Map<String, Object> shortPassword = validRegisterBody();
        shortPassword.put("password", "12345"); // 5 ký tự, biên dưới không hợp lệ

        Map<String, Object> badPhone = validRegisterBody();
        badPhone.put("phoneNumber", "123");

        return Stream.of(
                Arguments.of("thiếu email", missingEmail),
                Arguments.of("email sai định dạng", malformedEmail),
                Arguments.of("password 5 ký tự", shortPassword),
                Arguments.of("phoneNumber sai định dạng VN", badPhone)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidRegisterBodies")
    void should_return400ValidationFailed_when_registerBodyInvalid(String label, Map<String, Object> body) throws Exception {
        mockMvc.perform(post(USERS + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));
    }

    // ---- item 6: logout -> 204 ----

    @Test
    void should_return204_when_logout() throws Exception {
        mockMvc.perform(post(USERS + "/logout"))
                .andExpect(status().isNoContent());
    }

    // ---- item 7: refresh không có cookie -> 401 TOKEN_INVALID ----
    // Controller tự ném lỗi khi thiếu cookie, chưa hề gọi tới AuthService -- kiểm được ở
    // Phase 3 dù service bị mock.

    @Test
    void should_return401TokenInvalid_when_refreshCookieMissing() throws Exception {
        mockMvc.perform(post(USERS + "/refresh"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("TOKEN_INVALID"));
    }
}
