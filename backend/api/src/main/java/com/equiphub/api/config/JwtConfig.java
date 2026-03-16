package com.equiphub.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtConfig {
    
    private String secret;
    private Long accessTokenExpiration = 86400000L; // 24 hours default
    private Long refreshTokenExpiration = 604800000L; // 7 days default
    private String issuer;
    private String audience;

    /**
     * Alias so existing code calling getExpiration() still works.
     * Maps to jwt.access-token-expiration in YAML.
     */
    public Long getExpiration() {
        return accessTokenExpiration;
    }

    /**
     * Alias so existing code calling getRefreshExpiration() still works.
     * Maps to jwt.refresh-token-expiration in YAML.
     */
    public Long getRefreshExpiration() {
        return refreshTokenExpiration;
    }
    
    // Header names
    private String headerName = "Authorization";
    private String tokenPrefix = "Bearer ";
    
    // Cookie settings (optional)
    private boolean useCookie = false;
    private String cookieName = "equiphub-token";
    private int cookieMaxAge = 86400; // 24 hours in seconds

}
