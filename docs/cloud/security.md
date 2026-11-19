# Cloud Infrastructure Security & Hardening — TexSetu

## 1. Network Perimeter & Ingress Security

### 1.1 Web Application Firewall (WAF) & DDoS Protection
- **Cloudflare Edge Shield**: Ingress traffic passes through Cloudflare with automated DDoS mitigation, IP threat intelligence scoring, and bot protection.
- **Rate-Limiting Rules**:
  - `/auth/login`: Maximum 5 attempts per IP per minute (blocks credential stuffing).
  - `/rfqs/create`: Maximum 20 submissions per user per hour.
  - `/api/discovery`: Maximum 120 searches per minute.

### 1.2 Virtual Private Cloud (VPC) Subnet Isolation
- **Public Subnet**: Contains only Load Balancer and NAT Gateway interfaces.
- **Private Application Subnet**: Hosts Docker container instances running the Spring Boot application (no public IP addresses assigned).
- **Private Database Subnet**: Hosts managed PostgreSQL instances accessible strictly from the application subnet via Security Group rules on port 5432.

---

## 2. Operating System & Container Hardening

### 2.1 Principle of Least Privilege
- Docker containers run under dedicated UID `1001` (`texsetu`). No root access is permitted inside the runtime container.
- Container filesystem is mounted read-only except for the explicit upload directory `/var/texsetu/uploads`.

### 2.2 Secret & Credential Management
- Database passwords, JWT secret keys, and cloud storage tokens are injected strictly at runtime via environment variables (`.env` or AWS Secrets Manager / Doppler).
- Zero secrets are committed to Git repositories (`.gitignore` enforces `.env` exclusion).

---

## 3. Application Security Controls

### 3.1 Security Headers Configuration
The reverse proxy and Spring Security emit the following defense headers:
- `Content-Security-Policy (CSP)`: Restricts script execution to approved CDNs (Bootstrap, Bootstrap Icons).
- `X-Content-Type-Options: nosniff`: Prevents MIME-type sniffing attacks.
- `X-Frame-Options: SAMEORIGIN`: Defends against clickjacking.
- `Strict-Transport-Security (HSTS)`: Enforces HTTPS communication for 1 year (`max-age=31536000; includeSubDomains`).

### 3.2 SQL Injection & XSS Defenses
- **SQL Injection**: Complete prevention achieved via Spring Data JPA and Hibernate parameterized queries. Zero raw string concatenation in SQL queries.
- **Cross-Site Scripting (XSS)**: Thymeleaf automatically HTML-encodes all dynamic output (`th:text`) by default, neutralizing malicious script injection from user inputs.
