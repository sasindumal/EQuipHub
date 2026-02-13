package com.equiphub.api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "EQuipHub API v3.8",
        version = "3.8.0",
        description = """
            Equipment Request Management System for Computer Engineering & Electrical Engineering Departments.
            
            **Features:**
            - JWT Authentication & OAuth2 (Google/GitHub)
            - 5 Request Types: Lab Session, Coursework, Research, Extracurricular, Personal
            - Multi-stage Approval Workflow
            - Equipment Management & Condition Tracking
            - Penalty System with Appeals
            - Role-based Access Control (7 roles)
            
            **Authentication:**
            1. Get JWT token from `/api/v1/auth/login`
            2. Click 'Authorize' button above
            3. Enter: `Bearer <your-token>`
            """,
        contact = @Contact(
            name = "EQuipHub Development Team",
            email = "support@equiphub.edu",
            url = "https://github.com/your-org/equiphub"
        ),
        license = @License(
            name = "MIT License",
            url = "https://opensource.org/licenses/MIT"
        )
    ),
    servers = {
        @Server(
            url = "http://localhost:8080",
            description = "Local Development Server"
        ),
        @Server(
            url = "https://equiphub-api.onrender.com",
            description = "Production Server (Render)"
        )
    },
    security = {
        @SecurityRequirement(name = "bearerAuth")
    }
)
@SecurityScheme(
    name = "bearerAuth",
    description = "JWT Bearer Token Authentication. Login to get token.",
    scheme = "bearer",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
    // Configuration through annotations above
}
