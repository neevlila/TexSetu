# Docker & Containerization Guide — TexSetu

## 1. Multi-Stage Dockerfile Architecture

TexSetu employs a hardened multi-stage Docker build that isolates compiler tooling from the final lightweight production runtime image, minimizing image footprint and attack surface.

```
[Stage 1: Build & Package]
  • Base: maven:3.9.9-eclipse-temurin-21-alpine
  • Copies pom.xml, downloads dependencies (layer caching)
  • Compiles source code with Java 21 LTS release target
  • Produces optimized executable JAR file

         │ Copies only texsetu-app.jar
         ▼

[Stage 2: Production Runtime Environment]
  • Base: eclipse-temurin:21-jre-alpine (~180 MB)
  • Creates unprivileged user & group: texsetu (UID 1001)
  • Enforces non-root execution
  • Mounts volume for file uploads (/var/texsetu/uploads)
  • Runs with optimized JVM memory ergonomics
```

---

## 2. Dockerfile Specification

```dockerfile
# Stage 1: Build stage
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build

# Copy dependency descriptor first for efficient layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build production package
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Hardened Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create non-root system user and group
RUN addgroup -g 1001 texsetu && \
    adduser -u 1001 -G texsetu -s /bin/sh -D texsetu

# Create upload directory and grant permissions
RUN mkdir -p /var/texsetu/uploads && \
    chown -R texsetu:texsetu /var/texsetu

# Copy compiled artifact from builder stage
COPY --from=builder --chown=texsetu:texsetu /build/target/texsetu-*.jar app.jar

# Switch to unprivileged user
USER texsetu

# Expose HTTP port
EXPOSE 8080

# Configure production JVM flags
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

---

## 3. Docker Compose Orchestration

The `docker-compose.yml` file provisions the application along with a dedicated PostgreSQL 16 database for local multi-container development or on-premise production setups:

```yaml
version: '3.8'

services:
  db:
    image: postgres:16-alpine
    container_name: texsetu-db
    restart: unless-stopped
    environment:
      POSTGRES_DB: texsetudb
      POSTGRES_USER: texsetu
      POSTGRES_PASSWORD: texsetu_password
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"
    networks:
      - texsetu-net

  app:
    build: .
    container_name: texsetu-app
    restart: unless-stopped
    depends_on:
      - db
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/texsetudb
      SPRING_DATASOURCE_USERNAME: texsetu
      SPRING_DATASOURCE_PASSWORD: texsetu_password
      APP_STORAGE_DIR: /var/texsetu/uploads
    ports:
      - "8080:8080"
    volumes:
      - app_uploads:/var/texsetu/uploads
    networks:
      - texsetu-net

volumes:
  postgres_data:
  app_uploads:

networks:
  texsetu-net:
    driver: bridge
```
