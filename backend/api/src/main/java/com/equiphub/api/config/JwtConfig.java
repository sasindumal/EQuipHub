package com.equiphub.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtConfig {
    
    private String secret;
    private Long expiration; // milliseconds
    private Long refreshExpiration; // milliseconds
    private String issuer;
    private String audience;
    
    // Header names
    private String headerName = "Authorization";
    private String tokenPrefix = "Bearer ";
    
    // Cookie settings (optional)
    private boolean useCookie = false;
    private String cookieName = "equiphub-token";
    private int cookieMaxAge = 86400; // 24 hours in seconds
}
