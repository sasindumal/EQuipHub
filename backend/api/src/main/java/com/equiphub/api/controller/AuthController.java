package com.equiphub.api.controller;

import com.equiphub.api.dto.auth.AuthResponse;
import com.equiphub.api.dto.auth.LoginRequest;
import com.equiphub.api.dto.auth.RegisterRequest;
import com.equiphub.api.model.User;
import com.equiphub.api.repository.UserRepository;
import com.equiphub.api.security.CustomUserDetails;
import com.equiphub.api.security.jwt.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and registration endpoints")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            log.info("Login attempt for user: {}", loginRequest.getEmail());
            
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
                )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Generate JWT token
            String jwt = jwtUtils.generateToken(authentication);

            // Get user details
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            log.info("User {} logged in successfully", loginRequest.getEmail());

            // Return response
            AuthResponse response = new AuthResponse();
            response.setToken(jwt);
            response.setTokenType("Bearer");
            response.setUserId(userDetails.getUserId());
            response.setEmail(userDetails.getEmail());
            response.setFirstName(userDetails.getFirstName());
            response.setLastName(userDetails.getLastName());
            response.setRole(userDetails.getRole().name());
            response.setExpiresIn(jwtUtils.getExpirationDateFromToken(jwt));

            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Login failed for user {}: {}", loginRequest.getEmail(), e.getMessage());
            
            Map<String, String> error = new HashMap<>();
            error.put("error", "Authentication failed");
            error.put("message", "Invalid email or password");
            error.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @PostMapping("/register")
    @Operation(summary = "User registration", description = "Register a new user")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            log.info("Registration attempt for email: {}", registerRequest.getEmail());
            
            // Check if email exists
            if (userRepository.existsByEmail(registerRequest.getEmail())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Registration failed");
                error.put("message", "Email is already in use!");
                return ResponseEntity.badRequest().body(error);
            }

            // Create new user
            User user = new User();
            user.setEmail(registerRequest.getEmail());
            user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
            user.setFirstName(registerRequest.getFirstName());
            user.setLastName(registerRequest.getLastName());
            user.setRole(User.Role.valueOf(registerRequest.getRole()));
            user.setStatus(User.Status.ACTIVE);
            user.setEmailVerified(false); // Will be verified later

            userRepository.save(user);
            
            log.info("User registered successfully: {}", registerRequest.getEmail());

            Map<String, String> response = new HashMap<>();
            response.put("message", "User registered successfully! Please verify your email.");
            response.put("email", user.getEmail());
            response.put("userId", user.getUserId().toString());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            log.error("Registration failed: {}", e.getMessage());
            
            Map<String, String> error = new HashMap<>();
            error.put("error", "Registration failed");
            error.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get currently authenticated user details")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "User not authenticated"));
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        
        Map<String, Object> response = new HashMap<>();
        response.put("userId", userDetails.getUserId());
        response.put("email", userDetails.getEmail());
        response.put("firstName", userDetails.getFirstName());
        response.put("lastName", userDetails.getLastName());
        response.put("role", userDetails.getRole().name());
        response.put("status", userDetails.getStatus().name());
        response.put("departmentId", userDetails.getDepartmentId());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Generate new JWT token from valid token")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = jwtUtils.extractTokenFromHeader(authHeader);
            
            if (token == null || !jwtUtils.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid or expired token"));
            }

            String email = jwtUtils.getEmailFromToken(token);
            String role = jwtUtils.getRoleFromToken(token);
            
            // Generate new token
            String newToken = jwtUtils.generateTokenFromUserDetails(
                jwtUtils.getUserIdFromToken(token),
                email,
                role
            );

            Map<String, Object> response = new HashMap<>();
            response.put("token", newToken);
            response.put("tokenType", "Bearer");
            response.put("expiresIn", jwtUtils.getExpirationDateFromToken(newToken));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Token refresh failed", "message", e.getMessage()));
        }
    }
}
