# Production Cloud Deployment Guide — TexSetu

## 1. Prerequisites & Target Environment
- **Host System**: Ubuntu 22.04 LTS / Debian 12 / AWS Linux 2023.
- **Runtime**: Docker Engine 24.0+ and Docker Compose v2.20+.
- **Database**: PostgreSQL 16 (AWS RDS, Supabase, or containerized).
- **Domain & SSL**: Valid FQDN (e.g., `app.texsetu.com`) pointed to cloud server with Let's Encrypt / Cloudflare SSL certificate.

---

## 2. Zero-Downtime Deployment Steps

### Step 1: Clone Repository & Configure Environment
```bash
git clone https://github.com/texsetu/texsetu-platform.git /opt/texsetu
cd /opt/texsetu

# Copy production environment configuration template
cp .env.example .env

# Edit environment variables with production secrets
nano .env
```

Ensure the following variables are configured in `.env`:
```env
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080
SPRING_DATASOURCE_URL=jdbc:postgresql://db.host.internal:5432/texsetudb
SPRING_DATASOURCE_USERNAME=texsetu_admin
SPRING_DATASOURCE_PASSWORD=YourStrongSecurePassword123!
JWT_SECRET=c2VjdXJlX2xvbmdfc3ByaW5nX2Jvb3RfMjU2X2JpdF9zZWNyZXRfa2V5
APP_STORAGE_DIR=/var/texsetu/uploads
```

### Step 2: Build & Start Services with Docker Compose
```bash
# Build multi-stage Docker image and start container detached
docker compose up -d --build

# Verify container status
docker compose ps

# Inspect application boot logs
docker compose logs -f app
```

### Step 3: Verify Application Health
```bash
# Probe Spring Boot Actuator Health Endpoint
curl -f http://localhost:8080/actuator/health

# Expected response:
# {"status":"UP","components":{"db":{"status":"UP"},"diskSpace":{"status":"UP"}}}
```

### Step 4: Configure Nginx Reverse Proxy & SSL (Optional)
```nginx
server {
    listen 80;
    server_name app.texsetu.com;
    return 301 https://$host$request_uri;
}

server {
    listen 443 ssl http2;
    server_name app.texsetu.com;

    ssl_certificate /etc/letsencrypt/live/app.texsetu.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/app.texsetu.com/privkey.pem;

    client_max_body_size 12M;

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

---

## 3. Rolling Updates & Hot-Patching
To deploy an updated release without dropping in-flight user sessions:
```bash
git pull origin main
docker compose build app
docker compose up -d --no-deps app
```
Spring Boot’s graceful shutdown setting (`server.shutdown=graceful` in `application.yml`) will allow existing transactions up to 30 seconds to finish before stopping the old container.
