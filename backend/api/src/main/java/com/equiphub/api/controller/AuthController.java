package com.equiphub.api.controller;

import com.equiphub.api.dto.auth.AuthResponse;
import com.equiphub.api.dto.auth.LoginRequest;
import com.equiphub.api.dto.auth.RegisterRequest;
import com.equiphub.api.dto.auth.VerifyEmailRequest;
import com.equiphub.api.model.User;
import com.equiphub.api.repository.UserRepository;
import com.equiphub.api.security.CustomUserDetails;
import com.equiphub.api.security.jwt.JwtUtils;
import com.equiphub.api.service.EmailVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.UUID;

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
    private final EmailVerificationService emailVerificationService;

    // University email pattern: 20xxExxx@eng.jfn.ac.lk
    private static final Pattern UNIVERSITY_EMAIL_PATTERN = 
        Pattern.compile("^20\\d{2}[A-Za-z]\\d{3}@eng\\.jfn\\.ac\\.lk$");

@PostMapping("/register")
@Operation(
    summary = "Student registration",
    description = "Self-registration for students only. University email required."
)
    public ResponseEntity<Map<String, Object>> register(
            @Valid @RequestBody RegisterRequest registerRequest) {
        try {
            log.info("Registration attempt for email: {}", registerRequest.getEmail());

            // 1. Validate university email format
            if (!UNIVERSITY_EMAIL_PATTERN.matcher(registerRequest.getEmail()).matches()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid email format",
                    "message", "Email must be in format: 20xxExxx@eng.jfn.ac.lk (e.g., 2021E001@eng.jfn.ac.lk)"
                ));
            }

            // 2. Check if email already exists
            if (userRepository.existsByEmail(registerRequest.getEmail())) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Registration failed",
                    "message", "Email is already registered!"
                ));
            }

            // 3. Check if index number already exists
            if (userRepository.existsByIndexNumber(registerRequest.getIndexNumber())) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Registration failed",
                    "message", "Index number is already registered!"
                ));
            }

            // 4. Create new STUDENT user — role is hardcoded, never from request
            User user = new User();
            user.setEmail(registerRequest.getEmail());
            user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
            user.setFirstName(registerRequest.getFirstName());
            user.setLastName(registerRequest.getLastName());
            user.setRole(User.Role.STUDENT);           // ✅ Always STUDENT
            user.setStatus(User.Status.ACTIVE);
            user.setEmailVerified(false);
            user.setSemesterYear(registerRequest.getSemesterYear());
            user.setIndexNumber(registerRequest.getIndexNumber().toUpperCase());

            userRepository.save(user);

            // 5. Send verification email
            emailVerificationService.generateAndSendVerificationCode(user);

            log.info("Student registered successfully: {}", registerRequest.getEmail());

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Registration successful! Please check your email for verification code.",
                "email", user.getEmail(),
                "userId", user.getUserId().toString(),
                "role", "STUDENT",
                "nextStep", "Verify your email using the code sent to " + user.getEmail()
            ));

        } catch (Exception e) {
            log.error("Registration failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Registration failed",
                "message", e.getMessage()
            ));
        }
    }


    @PostMapping("/verify-email")
    @Operation(summary = "Verify email", description = "Verify email with 6-digit code")
    public ResponseEntity<?> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        try {
            log.info("Email verification attempt for: {}", request.getEmail());

            User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

            if (user.getEmailVerified()) {
                return ResponseEntity.ok(Map.of(
                    "message", "Email already verified",
                    "verified", true
                ));
            }

            boolean verified = emailVerificationService.verifyCode(user, request.getCode());

            if (verified) {
                log.info("Email verified successfully for: {}", request.getEmail());
                return ResponseEntity.ok(Map.of(
                    "message", "Email verified successfully! You can now login.",
                    "verified", true
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Verification failed",
                    "message", "Invalid or expired verification code. Please request a new code."
                ));
            }

        } catch (Exception e) {
            log.error("Email verification failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Verification failed", "message", e.getMessage()));
        }
    }

    @PostMapping("/resend-code")
    @Operation(summary = "Resend verification code", description = "Resend email verification code")
    public ResponseEntity<?> resendVerificationCode(@RequestParam String email) {
        try {
            log.info("Resend verification code request for: {}", email);

            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

            if (user.getEmailVerified()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Already verified",
                    "message", "Email is already verified"
                ));
            }

            emailVerificationService.resendVerificationCode(user);

            return ResponseEntity.ok(Map.of(
                "message", "Verification code sent successfully! Check your email.",
                "email", email
            ));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to resend code",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Failed to resend verification code: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Server error", "message", e.getMessage()));
        }
    }

    

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            log.info("Login attempt for user: {}", loginRequest.getEmail());

            // Check if user exists and is verified
            User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

            // Check if email is verified (PRODUCTION MODE)
            // Uncomment in production after testing
            
            // if (!user.getEmailVerified()) {
            //     return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
            //         "error", "Email not verified",
            //         "message", "Please verify your email before logging in",
            //         "email", user.getEmail()
            //     ));
            // }
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

        } catch (DisabledException e) {
            log.error("Account disabled for user: {}", loginRequest.getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "error", "Account disabled",
                "message", "Your account has been disabled. Please contact administrator."
            ));
        } catch (Exception e) {
            log.error("Login failed for user {}: {}", loginRequest.getEmail(), e.getMessage());

            Map<String, String> error = new HashMap<>();
            error.put("error", "Authentication failed");
            error.put("message", "Invalid email or password");
            error.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @GetMapping("/me")
    @Operation(
        summary = "Get current user", 
        description = "Get currently authenticated user details. Requires valid JWT token."
    )
    @SecurityRequirement(name = "bearerAuth") // ← Add this annotation
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        try {
            // Check authentication
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                        "error", "User not authenticated",
                        "message", "Please login and provide a valid JWT token",
                        "hint", "Click 'Authorize' button and enter: Bearer YOUR_TOKEN"
                    ));
            }

            // Check if principal is anonymous
            if (authentication.getPrincipal() instanceof String 
                && "anonymousUser".equals(authentication.getPrincipal())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                        "error", "Anonymous user",
                        "message", "Please login first"
                    ));
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
            response.put("emailVerified", userDetails.isEmailVerified());

            return ResponseEntity.ok(response);
            
        } catch (ClassCastException e) {
            log.error("Invalid principal type: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                    "error", "Invalid authentication",
                    "message", "Please login again"
                ));
        } catch (Exception e) {
            log.error("Error getting current user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Server error", "message", e.getMessage()));
        }
    }


    @PostMapping("/refresh")
    @Operation(summary = "Refresh JWT token", 
    description = "Generate new JWT token from valid token. Pass token in Authorization header.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> refreshToken(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            String token = jwtUtils.extractTokenFromHeader(authHeader);

            if (token == null || !jwtUtils.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                    "error", "Missing Authorization Header",
                    "message", "Please provide Authorization header with Bearer token",
                    "example", "Authorization: Bearer YOUR_JWT_TOKEN"
                ));
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
