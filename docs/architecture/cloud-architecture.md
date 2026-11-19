# Cloud Architecture & Deployment Topography — TexSetu

## 1. Cloud-Native Production Topography

TexSetu is designed for high-availability, containerized cloud deployment across modern cloud platforms (AWS, Google Cloud Platform, or DigitalOcean / Railway / Render).

```
                            [Public Internet]
                                    │
                                    ▼
                [Cloudflare CDN & Edge DDoS Protection]
                                    │
                          HTTPS / TLS 1.3 (Port 443)
                                    │
                                    ▼
            [Cloud Load Balancer / Reverse Proxy (Nginx / ALB)]
                                    │
                                    ▼
                    ┌───────────────────────────────┐
                    │  Docker Container Cluster     │
                    │  (Spring Boot 3.3 / Java 21)  │
                    │                               │
                    │  • Non-root secure runtime    │
                    │  • Health checks (/actuator)  │
                    │  • Thread pool: 200 workers   │
                    └───────┬───────────────┬───────┘
                            │               │
                            │ JDBC          │ S3 API / Storage Driver
                            ▼               ▼
          [Managed PostgreSQL 16 DB]    [Object Storage (AWS S3 / Supabase)]
          (Multi-AZ, Point-in-time)     (Challan PDFs, Inspection Photos)
```

---

## 2. Component Design & Security Isolation

### 2.1 Edge & Ingress Tier
- **Edge CDN (Cloudflare)**: Provides Web Application Firewall (WAF) filtering, rate-limiting against brute-force login attempts, and edge caching for static assets (CSS, JS, SVG icons).
- **Reverse Proxy**: Enforces TLS 1.3 termination, adds security headers (`Strict-Transport-Security`, `X-Content-Type-Options: nosniff`, `X-Frame-Options: SAMEORIGIN`), and routes incoming traffic to internal private subnet container endpoints.

### 2.2 Application Tier (Dockerized Spring Boot 3)
- **Container Security**: The application runs inside a lightweight Alpine Linux / Eclipse Temurin multi-stage container under a dedicated, non-privileged system user (`texsetu:texsetu`, UID 1001).
- **Graceful Shutdown**: Enabled in `application.yml` (`server.shutdown: graceful`) to allow running transactions to complete before terminating instances during rolling updates.
- **Observability**: Health endpoints (`/actuator/health`) and application metrics (`/actuator/metrics`) provide real-time liveness and readiness probes for container orchestrators (Kubernetes / ECS).

### 2.3 Persistence Tier (PostgreSQL 16)
- **High-Availability Configuration**: Managed PostgreSQL with a synchronous primary and asynchronous read-replica across multiple Availability Zones (AZ).
- **Automated Snapshots & Point-In-Time Recovery (PITR)**: 30-day continuous WAL archiving ensuring Recovery Point Objective (RPO) < 5 minutes and Recovery Time Objective (RTO) < 1 hour.
- **Connection Pooling**: Integrated HikariCP pool sized to handle 50 concurrent transactions per container instance with leak detection enabled.

### 2.4 Object Storage Tier (S3 / Supabase Storage)
- **Challan & Inspection Artifacts**: Inward delivery challan scans, inspection photographs, and business registration certificates are stored in private cloud object buckets.
- **Pre-Signed URL Access**: File downloads are served via expiring pre-signed URLs or streaming controller endpoints, preventing public bucket exposure.
