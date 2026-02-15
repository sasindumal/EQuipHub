package com.equiphub.api.config;

import com.equiphub.api.security.jwt.JwtAuthenticationEntryPoint;
import com.equiphub.api.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Authorization rules (paths WITHOUT /api/v1 prefix)
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - NO AUTHENTICATION REQUIRED
                .requestMatchers(
                    "/auth/**",           // Auth endpoints
                    "/public/**",         // Public endpoints
                    "/swagger-ui/**",     // Swagger UI
                    "/swagger-ui.html",   // Swagger UI HTML
                    "/v3/api-docs/**",    // OpenAPI docs
                    "/api-docs/**",       // OpenAPI docs
                    "/swagger-resources/**",
                    "/webjars/**",
                    "/actuator/health",   // Health check
                    "/actuator/info"      // Info endpoint
                ).permitAll()
                
                // OAuth2 endpoints (if used later)
                .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                
                // Admin endpoints - SYSTEM_ADMIN only
                .requestMatchers("/admin/**").hasRole("SYSTEMADMIN")
                
                // Department Admin endpoints
                .requestMatchers("/department-admin/**")
                    .hasAnyRole("DEPARTMENTADMIN", "HEADOFDEPARTMENT", "SYSTEMADMIN")
                
                // Technical Officer endpoints
                .requestMatchers("/equipment/manage/**")
                    .hasAnyRole("TECHNICALOFFICER", "DEPARTMENTADMIN", "SYSTEMADMIN")
                
                // Approval endpoints - Lecturers and above
                .requestMatchers("/approvals/**")
                    .hasAnyRole("LECTURER", "HEADOFDEPARTMENT", "DEPARTMENTADMIN", "SYSTEMADMIN")
                
                // Student endpoints
                .requestMatchers("/requests/student/**")
                    .hasAnyRole("STUDENT", "INSTRUCTOR", "LECTURER")
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            
            // Session management - stateless (JWT)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Exception handling
            .exceptionHandling(exception ->
                exception.authenticationEntryPoint(jwtAuthenticationEntryPoint)
            )
            
            // Authentication provider
            .authenticationProvider(authenticationProvider())
            
            // JWT filter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // strength 12 for production
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",
            "http://localhost:5173",
            "http://127.0.0.1:3000",
            "http://127.0.0.1:5173"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
