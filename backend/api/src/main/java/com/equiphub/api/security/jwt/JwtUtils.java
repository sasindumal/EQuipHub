package com.equiphub.api.security.jwt;

import com.equiphub.api.config.JwtConfig;
import com.equiphub.api.security.CustomUserDetails;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtils {

    private final JwtConfig jwtConfig;
    private SecretKey key;

    /**
     * Generate secret key from configuration
     */
    @PostConstruct
    public void init() {
        // Ensure key is at least 256 bits for HS256
        this.key = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
        log.info("JWT signing key initialized");
    }

    /**
     * Generate JWT token from Authentication object
     */
    public String generateToken(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userDetails.getUserId().toString());
        claims.put("email", userDetails.getEmail());
        claims.put("role", userDetails.getRole().name());
        claims.put("firstName", userDetails.getFirstName());
        claims.put("lastName", userDetails.getLastName());
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtConfig.getExpiration());

        return Jwts.builder()
                .subject(userDetails.getEmail()) // Use email as subject
                .claims(claims)
                .issuedAt(now)
                .expiration(expiryDate)
                .issuer(jwtConfig.getIssuer())
                .signWith(key)
                .compact();
    }

    /**
     * Generate JWT token from user ID, email and role
     */
    public String generateTokenFromUserDetails(UUID userId, String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId.toString());
        claims.put("role", role);
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtConfig.getExpiration());

        return Jwts.builder()
                .subject(email) // Email as subject
                .claims(claims)
                .issuedAt(now)
                .expiration(expiryDate)
                .issuer(jwtConfig.getIssuer())
                .signWith(key)
                .compact();
    }

    /**
     * Generate refresh token with longer expiration
     */
    public String generateRefreshToken(UUID userId, String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtConfig.getRefreshExpiration());

        return Jwts.builder()
                .subject(email)
                .claim("userId", userId.toString())
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(expiryDate)
                .issuer(jwtConfig.getIssuer())
                .signWith(key)
                .compact();
    }

    /**
     * Get user ID from JWT token
     */
    public UUID getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return UUID.fromString(claims.get("userId", String.class));
    }

    /**
     * Get email from JWT token (from subject)
     */
    public String getEmailFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getSubject(); // Email is in subject
    }

    /**
     * Get role from JWT token
     */
    public String getRoleFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("role", String.class);
    }

    /**
     * Get all claims from JWT token
     */
    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Validate JWT token
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SignatureException ex) {
            log.error("Invalid JWT signature: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException ex) {
            return true;
        }
    }

    /**
     * Get token expiration date
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getExpiration();
    }

    /**
     * Extract token from Authorization header
     */
    public String extractTokenFromHeader(String header) {
        if (header != null && header.startsWith(jwtConfig.getTokenPrefix())) {
            return header.substring(jwtConfig.getTokenPrefix().length());
        }
        return null;
    }
}
