package com.equiphub.api.dto.auth;

import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
public class AuthResponse {
    private String token;
    private String tokenType;
    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private Date expiresIn;
}
