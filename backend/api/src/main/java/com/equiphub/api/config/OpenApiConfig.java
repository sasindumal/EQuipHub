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
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import java.util.List;

import org.springframework.context.annotation.Bean;
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

    @Bean
    public OpenAPI customOpenAPI() {
        // NOTE: Do NOT append server.servlet.context-path (/api/v1) to the server URL here.
        // Spring Boot automatically prepends the context-path to every incoming request.
        // Adding it here as well causes Swagger to send requests to /api/v1/api/v1/* which
        // results in NoResourceFoundException (500). Use bare host URLs only.
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("EQuipHub API v3.8")
                        .description("""
                                Equipment Request Management System API
                                
                                **Authentication:**
                                1. Register: POST /auth/register
                                2. Verify Email: POST /auth/verify-email
                                3. Login: POST /auth/login (Get JWT token)
                                4. Click "Authorize" button above and enter: Bearer YOUR_TOKEN
                                
                                **University Email Format:** 20xxExxx@eng.jfn.ac.lk
                                
                                **Example:** 2021E001@eng.jfn.ac.lk
                                """)
                        .version("3.8.0")
                        .contact(new io.swagger.v3.oas.models.info.Contact()
                                .name("EQuipHub Support")
                                .email("support@equiphub.ac.lk")
                                .url("https://equiphub.ac.lk"))
                        .license(new io.swagger.v3.oas.models.info.License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                // Bare host only — context-path (/api/v1) is applied by Spring Boot automatically
                .servers(List.of(
                        new io.swagger.v3.oas.models.servers.Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server"),
                        new io.swagger.v3.oas.models.servers.Server()
                                .url("https://api.equiphub.ac.lk")
                                .description("Production Server")
                ))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new io.swagger.v3.oas.models.security.SecurityScheme()
                                .name(securitySchemeName)
                                .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter JWT token obtained from /auth/login")))
                .addSecurityItem(new io.swagger.v3.oas.models.security.SecurityRequirement().addList(securitySchemeName));
    }
}
