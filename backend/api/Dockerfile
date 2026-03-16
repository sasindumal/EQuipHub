# ==================== DOCKERFILE CONFIGURATION ====================
# Location: backend/api/Dockerfile
# Purpose: Build and run Spring Boot application in Docker container
# Build: docker build -t equiphub-api:latest .
# Run: docker run -d --name equiphub-api -p 8080:8080 equiphub-api:latest

# ==================== STAGE 1: BUILD ====================
FROM maven:3.9-eclipse-temurin-21 AS builder

LABEL stage=builder

WORKDIR /app

# Set environment variables
ENV MAVEN_CONFIG=/root/.m2

# Copy pom.xml and download dependencies
# This layer is cached as long as pom.xml doesn't change
COPY pom.xml .

# Download all dependencies (will be cached)
RUN mvn dependency:go-offline -B -DskipTests=true

# Copy source code
COPY src ./src

# Build the application (skip tests for faster build)
RUN mvn clean package -DskipTests -B

# ==================== STAGE 2: RUNTIME ====================
FROM eclipse-temurin:21-jdk-alpine

LABEL maintainer="equiphub-team@example.com"
LABEL version="1.0.0"
LABEL description="EQuipHub - Equipment Request Management System"

WORKDIR /app

# Install required tools
RUN apk add --no-cache \
    curl \
    ca-certificates \
    tzdata \
    && rm -rf /var/cache/apk/*

# Set timezone
ENV TZ=Asia/Kolkata
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Copy JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Create non-root user for security
RUN addgroup -g 1000 appuser && \
    adduser -D -u 1000 -G appuser appuser && \
    chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/api/actuator/health || exit 1

# JVM performance tuning
ENV JAVA_OPTS="\
  -Xms512m \
  -Xmx1024m \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+DisableExplicitGC \
  -XX:+AlwaysPreTouch \
  -XX:+ParallelRefProcEnabled \
  -XX:+UnlockDiagnosticVMOptions \
  -XX:G1SummarizeRSetStatsPeriod=1 \
  -Djava.awt.headless=true \
  -Dspring.profiles.active=prod"

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
