package com.equiphub.api.controller;

import com.equiphub.api.dto.auth.*;
import com.equiphub.api.model.User;
import com.equiphub.api.repository.UserRepository;
import com.equiphub.api.security.CustomUserDetails;
import com.equiphub.api.security.jwt.JwtUtils;
import com.equiphub.api.service.EmailVerificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private AuthenticationManager authenticationManager;
    @MockBean private UserRepository userRepository;
    @MockBean private PasswordEncoder passwordEncoder;
    @MockBean private JwtUtils jwtUtils;
    @MockBean private EmailVerificationService emailVerificationService;

    private static final String VALID_EMAIL = "2021E001@eng.jfn.ac.lk";

    @Test
    @DisplayName("POST /auth/register — valid university email → 201")
    void register_ValidEmail_Returns201() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setEmail(VALID_EMAIL);
        req.setPassword("Password123!");
        req.setIndexNumber("EG/2021/001");

        when(userRepository.existsByEmail(VALID_EMAIL)).thenReturn(false);
        when(userRepository.existsByIndexNumber(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");

        User saved = new User();
        saved.setUserId(UUID.randomUUID());
        saved.setEmail(VALID_EMAIL);
        when(userRepository.save(any(User.class))).thenReturn(saved);
        doNothing().when(emailVerificationService)
                   .generateAndSendVerificationCode(any(User.class));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /auth/register — non-university email → 400")
    void register_InvalidEmail_Returns400() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setEmail("john@gmail.com");
        req.setPassword("Password123!");
        req.setIndexNumber("EG/2021/001");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/register — duplicate email → 400")
    void register_DuplicateEmail_Returns400() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setEmail(VALID_EMAIL);
        req.setPassword("Password123!");
        req.setIndexNumber("EG/2021/001");

        when(userRepository.existsByEmail(VALID_EMAIL)).thenReturn(true);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Registration failed"));
    }

    @Test
    @DisplayName("POST /auth/login — valid credentials → 200 with token")
    void login_ValidCredentials_ReturnsToken() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail(VALID_EMAIL);
        req.setPassword("Password123!");

        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setEmail(VALID_EMAIL);
        user.setRole(User.Role.STUDENT);

        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        when(userDetails.getUserId()).thenReturn(user.getUserId());
        when(userDetails.getEmail()).thenReturn(VALID_EMAIL);
        when(userDetails.getFirstName()).thenReturn("John");
        when(userDetails.getLastName()).thenReturn("Doe");
        when(userDetails.getRole()).thenReturn(User.Role.STUDENT);
        when(userDetails.getStatus()).thenReturn(User.Status.ACTIVE);

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(userDetails);

        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(jwtUtils.generateToken(any(Authentication.class))).thenReturn("mock-jwt");
        when(jwtUtils.getExpirationDateFromToken(anyString())).thenReturn(new Date());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt"));
    }

    @Test
    @DisplayName("POST /auth/verify-email — valid code → 200 verified=true")
    void verifyEmail_ValidCode_ReturnsVerified() throws Exception {
        VerifyEmailRequest req = new VerifyEmailRequest();
        req.setEmail(VALID_EMAIL);
        req.setCode("123456");

        User user = new User();
        user.setEmailVerified(false);

        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(user));
        when(emailVerificationService.verifyCode(any(User.class), anyString())).thenReturn(true);

        mockMvc.perform(post("/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").value(true));
    }

    @Test
    @DisplayName("POST /auth/resend-code — valid email → 200")
    void resendCode_ValidEmail_Returns200() throws Exception {
        User user = new User();
        user.setEmailVerified(false);

        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(user));
        doNothing().when(emailVerificationService)
                   .resendVerificationCode(any(User.class));

        mockMvc.perform(post("/auth/resend-code")
                        .param("email", VALID_EMAIL))
                .andExpect(status().isOk());
    }
}
