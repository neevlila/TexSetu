# ==============================================================================
# TEXSETU - MULTI-STAGE DOCKER BUILD FOR PRODUCTION CLOUD DEPLOYMENT (RENDER)
# Baseline: Java 21 LTS (Eclipse Temurin)
# ==============================================================================

# STAGE 1: BUILD
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app

# Copy Maven Wrapper & Project Descriptor
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Make wrapper executable & cache dependencies
RUN chmod +x ./mvnw || true
RUN ./mvnw dependency:go-offline -B || true

# Copy Source Code
COPY src ./src

# Package Production Jar without running unit tests during docker build (tests run in CI)
RUN ./mvnw clean package -DskipTests -B

# STAGE 2: RUNTIME CONTAINER
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Create non-root system user for security compliance
RUN groupadd -r texsetu && useradd -r -g texsetu -m -s /sbin/nologin texsetu

# Create directory for local uploads fallback and assign permissions
RUN mkdir -p /app/uploads && chown -R texsetu:texsetu /app

# Copy executable jar from builder stage
COPY --from=builder --chown=texsetu:texsetu /app/target/texsetu-1.0.0.jar /app/app.jar

USER texsetu

# Expose standard cloud port
EXPOSE 8080

# Environment defaults (override via Render / Docker Compose environment variables)
ENV SPRING_PROFILES_ACTIVE=prod
ENV PORT=8080

# Health check using Spring Boot Actuator
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:${PORT:-8080}/actuator/health || exit 1

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/app.jar"]
