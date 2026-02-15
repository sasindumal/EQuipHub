package com.equiphub.api.security.jwt;

import com.equiphub.api.config.JwtConfig;
import com.equiphub.api.security.CustomUserDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtConfig jwtConfig;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            // Extract JWT token from request
            String jwt = extractJwtFromRequest(request);

            // Validate token and set authentication
            if (StringUtils.hasText(jwt) && jwtUtils.validateToken(jwt)) {
                // Get email from token (not userId)
                String email = jwtUtils.getEmailFromToken(jwt);

                // Load user details by email
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

                // Create authentication object
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Set authentication in security context
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Set authentication for user: {}", email);
            }
        } catch (ExpiredJwtException ex) {
            log.error("JWT token is expired: {}", ex.getMessage());
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Token expired\", \"message\": \"JWT token has expired. Please login again.\"}");
            return; // Stop filter chain
        } catch (Exception ex) {
            log.error("Cannot set user authentication: {}", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from Authorization header
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(jwtConfig.getHeaderName());
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(jwtConfig.getTokenPrefix())) {
            return bearerToken.substring(jwtConfig.getTokenPrefix().length());
        }
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Skip filter for public endpoints
        return path.startsWith("/api/v1/auth/") ||
               path.startsWith("/api/v1/public/") ||
               path.startsWith("/api/v1/swagger-ui") ||
               path.startsWith("/api/v1/api-docs") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/actuator/health") ||
               path.startsWith("/oauth2/") ||
               path.startsWith("/login/oauth2/");
    }
}
