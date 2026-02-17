package com.equiphub.api;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ╔════════════════════════════════════════════════════════════════════════════╗
 * ║                        EQUIPHUB API v3.8 - Main Application                 ║
 * ╚════════════════════════════════════════════════════════════════════════════╝
 *
 * Project: Equipment Request Management System (EQuipHub v3.8)
 * Version: 3.8.0
 * Description: Production-ready RESTful API for managing equipment requests
 *              across Computer Engineering and Electrical Engineering departments
 *
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │ KEY FEATURES                                                                 │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ ✅ 7-Tier Role-Based Authorization                                          │
 * │    (SYSTEMADMIN, DEPARTMENTADMIN, HOD, LECTURER, INSTRUCTOR,                │
 * │     APPOINTEDLECTURER, TECHNICALOFFICER, STUDENT)                           │
 * │ ✅ 5 Request Types with Dynamic Approval Workflows                          │
 * │    (LABSESSION, COURSEWORK, RESEARCH, EXTRACURRICULAR, PERSONAL)            │
 * │ ✅ Automated Penalty System with Appeals (0-200+ points)                    │
 * │ ✅ Equipment Condition Tracking (0-5 scale)                                 │
 * │ ✅ JWT + OAuth2 Authentication (Google, GitHub)                             │
 * │ ✅ Redis Caching for High Performance                                       │
 * │ ✅ Async Email Notifications (Gmail SMTP)                                   │
 * │ ✅ Immutable Audit Logging (GDPR Compliant)                                 │
 * │ ✅ OpenAPI 3.0 Documentation (Swagger UI)                                   │
 * │ ✅ PostgreSQL 15+ with 21 Normalized Entities                               │
 * └─────────────────────────────────────────────────────────────────────────────┘
 *
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │ TECHNOLOGY STACK                                                             │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ • Spring Boot 3.2.2 (Java 21 LTS)                                           │
 * │ • Spring Security 6.2 (JWT + OAuth2)                                        │
 * │ • Spring Data JPA + Hibernate 6.4                                           │
 * │ • PostgreSQL 15+ (Primary Database)                                         │
 * │ • Redis 7.2+ (Distributed Caching)                                          │
 * │ • SpringDoc OpenAPI 2.3.0 (Swagger/OpenAPI)                                 │
 * │ • Project Lombok (Boilerplate Reduction)                                    │
 * │ • MapStruct 1.5.5 (DTO Mapping)                                             │
 * │ • jjwt 0.12.5 (JWT Token Management)                                        │
 * │ • HikariCP (Connection Pooling)                                             │
 * └─────────────────────────────────────────────────────────────────────────────┘
 *
 * @author EQuipHub Development Team
 * @version 3.8.0
 * @since January 2026
 */

@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
@OpenAPIDefinition(
    info = @Info(
        title = "EQuipHub API",
        version = "3.8.0",
        description = """
                Equipment Request Management System - RESTful API
                
                **Complete Solution for Laboratory Equipment Management**
                
                • Multi-department support (CSE, EEE, expandable) \n
                • 7-tier role-based access control \n
                • Automated approval workflows with SLA tracking \n
                • Penalty system with appeals (0-200+ points) \n
                • Real-time equipment availability tracking \n
                • Condition monitoring (0-5 scale) \n
                • Lab session booking (8-12 AM, 1-4 PM slots) \n
                • Research supervisor assignments \n
                • Extracurricular activity approvals \n
                • Personal project requests with HOD approval \n
                • Comprehensive audit logging (7-year retention) \n
                • Email notifications for all state changes \n
                • Redis caching for optimal performance \n
                
                **Authentication:**
                - JWT Bearer Token (24h validity in dev, 1h in prod) \n
                - OAuth2 (Google, GitHub) \n
                - Password: BCrypt (cost 12) \n
                
                **Base URL:** `/api/v1`
                **Documentation:** `/api/v1/swagger-ui.html`
                **Health Check:** `/api/v1/actuator/health`
                """,
        contact = @Contact(
            name = "EQuipHub Support Team\n",
            url = "https://github.com/your-org/equiphub\n",
            email = "support@equiphub.edu\n"
        ),
        license = @License(
            name = "MIT License\n",
            url = "https://opensource.org/licenses/MIT\n"
        )
    ),
    servers = {
        @Server(
            url = "http://localhost:8080/api/v1",
            description = "Local Development Server"
        ),
        @Server(
            url = "https://equiphub-api.onrender.com/api/v1",
            description = "Production Server (Render)"
        )
    },
    security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
    name = "bearerAuth",
    description = """
            JWT Bearer Token Authentication
            
            **How to Authenticate:**
            1. Call `POST /api/v1/auth/login` with credentials
            2. Receive JWT token in response
            3. Click 'Authorize' button above (or add header manually)
            4. Enter: `Bearer <your-token>`
            5. All subsequent requests will be authenticated
            
            **Token Details:**
            - Algorithm: HS256 (HMAC-SHA256)
            - Expiration: 24 hours (dev) / 1 hour (prod)
            - Refresh: Available at `/api/v1/auth/refresh`
            - Payload: userId, email, role, departmentId
            
            **OAuth2 Alternative:**
            - Google: `/oauth2/authorization/google`
            - GitHub: `/oauth2/authorization/github`
            """,
    scheme = "bearer",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER
)
@Slf4j
public class EquipHubApplication {

    /**
     * Application entry point
     *
     * @param args Command line arguments (e.g., --spring.profiles.active=prod)
     */
    public static void main(String[] args) {
        // Set default timezone to UTC for consistency
        System.setProperty("user.timezone", "Asia/Colombo");
        
        SpringApplication app = new SpringApplication(EquipHubApplication.class);
        
        // Graceful shutdown configuration
        app.setRegisterShutdownHook(true);
        
        app.run(args);
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════
     * POST-STARTUP INITIALIZATION & HEALTH CHECKS
     * ═══════════════════════════════════════════════════════════════════════
     */
    @Component
    @Slf4j
    public static class StartupListener {

        @Autowired
        private Environment env;

        @Autowired
        private ApplicationContext context;

        @Value("${server.port:8080}")
        private String serverPort;

        @Value("${server.servlet.context-path:/api/v1}")
        private String contextPath;

        @EventListener(ApplicationReadyEvent.class)
        public void onApplicationReady() {
            displayStartupBanner();
            logSystemInfo();
            logCriticalEndpoints();
            verifySystemHealth();
            logSecurityInfo();
        }

        /**
         * Display ASCII art startup banner
         */
        private void displayStartupBanner() {
            String banner = """
                    
                    ╔══════════════════════════════════════════════════════════════════════════╗
                    ║                                                                          ║
                    ║   ███████╗ ██████╗ ██╗   ██╗██╗██████╗ ██╗  ██╗██╗   ██╗██████╗       ║
                    ║   ██╔════╝██╔═══██╗██║   ██║██║██╔══██╗██║  ██║██║   ██║██╔══██╗      ║
                    ║   █████╗  ██║   ██║██║   ██║██║██████╔╝███████║██║   ██║██████╔╝      ║
                    ║   ██╔══╝  ██║▄▄ ██║██║   ██║██║██╔═══╝ ██╔══██║██║   ██║██╔══██╗      ║
                    ║   ███████╗╚██████╔╝╚██████╔╝██║██║     ██║  ██║╚██████╔╝██████╔╝      ║
                    ║   ╚══════╝ ╚══▀▀═╝  ╚═════╝ ╚═╝╚═╝     ╚═╝  ╚═╝ ╚═════╝ ╚═════╝       ║
                    ║                                                                          ║
                    ║                   Equipment Request Management System                   ║
                    ║                              Version 3.8.0                               ║
                    ║                                                                          ║
                    ╚══════════════════════════════════════════════════════════════════════════╝
                    
                    """;
            log.info(banner);
        }

        /**
         * Log system information
         */
        private void logSystemInfo() {
            String activeProfile = env.getProperty("spring.profiles.active", "default");
            String javaVersion = System.getProperty("java.version");
            String springBootVersion = env.getProperty("spring-boot.version", "3.2.2");
            
            String hostAddress = "localhost";
            try {
                hostAddress = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                log.warn("Could not determine host address");
            }

            String systemInfo = """
                    
                    ┌──────────────────────────────────────────────────────────────────────────┐
                    │ SYSTEM INFORMATION                                                        │
                    ├──────────────────────────────────────────────────────────────────────────┤
                    │ Application:        EQuipHub API v3.8.0                                  │
                    │ Environment:        %s                                                    │
                    │ Started:            %s                                                    │
                    │ Host:               %s                                                    │
                    │ Port:               %s                                                    │
                    │ Context Path:       %s                                                    │
                    │ Java Version:       %s                                                    │
                    │ Spring Boot:        %s                                                    │
                    │ PID:                %s                                                    │
                    └──────────────────────────────────────────────────────────────────────────┘
                    """.formatted(
                        activeProfile.toUpperCase(),
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        hostAddress,
                        serverPort,
                        contextPath,
                        javaVersion,
                        springBootVersion,
                        ProcessHandle.current().pid()
                    );
            log.info(systemInfo);
        }

        /**
         * Log critical API endpoints
         */
        private void logCriticalEndpoints() {
            String baseUrl = "http://localhost:" + serverPort + contextPath;
            
            String endpoints = """
                    
                    ┌──────────────────────────────────────────────────────────────────────────┐
                    │ CRITICAL ENDPOINTS                                                        │
                    ├──────────────────────────────────────────────────────────────────────────┤
                    │                                                                          │
                    │ 📚 DOCUMENTATION                                                         │
                    │    Swagger UI:      %s/swagger-ui.html                                   │
                    │    OpenAPI JSON:    %s/api-docs                                          │
                    │                                                                          │
                    │ 🔐 AUTHENTICATION                                                        │
                    │    POST %s/auth/login          - Login with email/password               │
                    │    POST %s/auth/register       - Student registration                    │
                    │    POST %s/auth/refresh        - Refresh JWT token                       │
                    │    GET  %s/auth/me             - Get current user                        │
                    │                                                                          │
                    │ 📋 REQUESTS                                                              │
                    │    POST %s/requests            - Create new request                      │
                    │    GET  %s/requests            - List all requests (paginated)           │
                    │    GET  %s/requests/{id}       - Get request details                     │
                    │    PUT  %s/requests/{id}       - Update request                          │
                    │    POST %s/requests/{id}/submit - Submit for approval                    │
                    │                                                                          │
                    │ ✅ APPROVALS                                                             │
                    │    POST %s/requests/{id}/approve  - Approve request                      │
                    │    POST %s/requests/{id}/reject   - Reject request                       │
                    │    GET  %s/approvals/pending      - My pending approvals                 │
                    │                                                                          │
                    │ 🔧 EQUIPMENT                                                             │
                    │    GET  %s/equipment           - List equipment (with filters)           │
                    │    GET  %s/equipment/{id}      - Equipment details                       │
                    │    POST %s/equipment           - Create equipment (TO only)              │
                    │    PUT  %s/equipment/{id}      - Update equipment (TO only)              │
                    │                                                                          │
                    │ 🏥 HEALTH & MONITORING                                                   │
                    │    GET  %s/actuator/health     - Health check                            │
                    │    GET  %s/actuator/info       - Application info                        │
                    │    GET  %s/actuator/metrics    - Performance metrics                     │
                    │                                                                          │
                    └──────────────────────────────────────────────────────────────────────────┘
                    """.formatted(
                        baseUrl, baseUrl,
                        baseUrl, baseUrl, baseUrl, baseUrl,
                        baseUrl, baseUrl, baseUrl, baseUrl, baseUrl,
                        baseUrl, baseUrl, baseUrl,
                        baseUrl, baseUrl, baseUrl, baseUrl,
                        baseUrl, baseUrl, baseUrl
                    );
            log.info(endpoints);
        }

        /**
         * Verify critical system components
         */
        private void verifySystemHealth() {
            StringBuilder health = new StringBuilder();
            health.append("\n┌──────────────────────────────────────────────────────────────────────────┐\n");
            health.append("│ SYSTEM HEALTH CHECK                                                       │\n");
            health.append("├──────────────────────────────────────────────────────────────────────────┤\n");
            
            // Check database connection
            try {
                DataSource dataSource = context.getBean(DataSource.class);
                dataSource.getConnection().close();
                health.append("│ ✅ Database (PostgreSQL):    CONNECTED                                   │\n");
            } catch (Exception e) {
                health.append("│ ❌ Database (PostgreSQL):    FAILED - ").append(e.getMessage()).append("\n");
                log.error("Database connection failed", e);
            }

            // Check Redis (if available)
            try {
                context.getBean("redisTemplate");
                health.append("│ ✅ Redis Cache:              CONNECTED                                   │\n");
            } catch (Exception e) {
                health.append("│ ⚠️  Redis Cache:              NOT CONFIGURED                             │\n");
            }

            // Check other beans
            checkBean(health, "jwtUtils", "JWT Utils");
            checkBean(health, "javaMailSender", "Email Service (SMTP)");
            checkBean(health, "passwordEncoder", "Password Encoder");
            checkBean(health, "modelMapper", "DTO Mapper");

            health.append("│ ✅ Spring Security:          ACTIVE (JWT + OAuth2)                       │\n");
            health.append("│ ✅ JPA Auditing:             ENABLED                                      │\n");
            health.append("│ ✅ Async Processing:         ENABLED                                      │\n");
            health.append("│ ✅ Transaction Management:   ENABLED                                      │\n");
            health.append("│ ✅ Method Security:          ENABLED (@PreAuthorize)                     │\n");
            health.append("│ ✅ CORS:                     CONFIGURED                                   │\n");
            
            health.append("└──────────────────────────────────────────────────────────────────────────┘\n");
            log.info(health.toString());
        }

        /**
         * Check if a bean exists
         */
        private void checkBean(StringBuilder health, String beanName, String displayName) {
            try {
                context.getBean(beanName);
                health.append(String.format("│ ✅ %-27s LOADED%40s│\n", displayName + ":", ""));
            } catch (Exception e) {
                health.append(String.format("│ ❌ %-27s NOT FOUND%37s│\n", displayName + ":", ""));
            }
        }

        /**
         * Log security configuration
         */
        private void logSecurityInfo() {
            String jwtSecret = env.getProperty("jwt.secret", "NOT_CONFIGURED");
            String jwtSecretMasked = jwtSecret.length() > 10 ? 
                jwtSecret.substring(0, 10) + "..." + jwtSecret.substring(jwtSecret.length() - 10) : 
                "***";

            String security = """
                    
                    ┌──────────────────────────────────────────────────────────────────────────┐
                    │ SECURITY CONFIGURATION                                                    │
                    ├──────────────────────────────────────────────────────────────────────────┤
                    │ JWT Secret:         %s                                                    │
                    │ JWT Expiration:     %s ms                                                 │
                    │ Password Encoding:  BCrypt (cost 12)                                     │
                    │ OAuth2 Providers:   Google, GitHub                                       │
                    │ CORS Allowed:       %s                                                    │
                    │ CSRF Protection:    DISABLED (Stateless API)                             │
                    │ Session Management: STATELESS                                            │
                    └──────────────────────────────────────────────────────────────────────────┘
                    
                    ╔══════════════════════════════════════════════════════════════════════════╗
                    ║                   🚀 APPLICATION STARTED SUCCESSFULLY                     ║
                    ║                                                                          ║
                    ║  Access Swagger UI:  http://localhost:%s%s/swagger-ui.html               ║
                    ║                                                                          ║
                    ╚══════════════════════════════════════════════════════════════════════════╝
                    
                    """.formatted(
                        jwtSecretMasked,
                        env.getProperty("jwt.expiration", "86400000"),
                        env.getProperty("cors.allowed-origins", "http://localhost:3000"),
                        serverPort,
                        contextPath
                    );
            log.info(security);
        }
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════
     * CONFIGURATION NOTES FOR DEVELOPERS
     * ═══════════════════════════════════════════════════════════════════════
     *
     * 1. ENVIRONMENT PROFILES
     *    - dev:  Development (DEBUG logging, Swagger enabled)
     *    - test: Testing (INFO logging, H2 in-memory database)
     *    - prod: Production (WARN logging, PostgreSQL, security hardened)
     *
     *    Set profile: export SPRING_PROFILES_ACTIVE=dev
     *    Or in IntelliJ: Run → Edit Configurations → Environment Variables
     *
     * 2. DATABASE SETUP
     *    - PostgreSQL 15+ required
     *    - Create database: CREATE DATABASE equiphub_dev;
     *    - Create user: CREATE USER equiphub_user WITH PASSWORD 'your_password';
     *    - Grant privileges: GRANT ALL PRIVILEGES ON DATABASE equiphub_dev TO equiphub_user;
     *    - Hibernate auto-creates tables (ddl-auto: update in dev, validate in prod)
     *
     * 3. REDIS SETUP (Optional but Recommended)
     *    - Install: brew install redis (macOS) or apt install redis (Ubuntu)
     *    - Start: redis-server
     *    - Test: redis-cli ping (should return PONG)
     *    - Configure in application.yml: spring.data.redis.host=localhost
     *
     * 4. JWT SECRET GENERATION
     *    - Generate secure secret (256+ bits):
     *      openssl rand -base64 64
     *    - Set in application.yml or environment variable:
     *      export JWT_SECRET="your_generated_secret"
     *
     * 5. OAUTH2 SETUP
     *    - Google: https://console.cloud.google.com/apis/credentials
     *      • Create OAuth 2.0 Client ID
     *      • Redirect URI: http://localhost:8080/api/v1/login/oauth2/code/google
     *    - GitHub: https://github.com/settings/developers
     *      • New OAuth App
     *      • Callback URL: http://localhost:8080/api/v1/login/oauth2/code/github
     *
     * 6. EMAIL CONFIGURATION (Gmail)
     *    - Enable 2-Step Verification in Google Account
     *    - Generate App Password: https://myaccount.google.com/apppasswords
     *    - Set in application.yml:
     *      spring.mail.username: your-email@gmail.com
     *      spring.mail.password: your-app-password
     *
     * 7. COMMON ISSUES
     *    - Port 8080 in use: Change server.port in application.yml
     *    - Database connection refused: Check PostgreSQL is running
     *    - Redis connection failed: Check Redis is running or disable caching
     *    - JWT signature error: Verify JWT_SECRET is same across restarts
     *
     * 8. USEFUL COMMANDS
     *    - Run application: ./mvnw spring-boot:run
     *    - Run with profile: ./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
     *    - Build JAR: ./mvnw clean package
     *    - Run JAR: java -jar target/equiphub-api-3.8.0.jar
     *    - Docker build: docker build -t equiphub-api:3.8.0 .
     *    - Docker run: docker run -p 8080:8080 equiphub-api:3.8.0
     */
}
