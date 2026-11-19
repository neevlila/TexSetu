# TEXSETU — B2B Textile Job-Work & Manufacturing Marketplace

[![Java 21](https://img.shields.io/badge/Java-21%20LTS-007396?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen)]()
[![Automated Tests](https://img.shields.io/badge/Tests-35%2F35%20Passed-success)]()
[![PostgreSQL](https://img.shields.io/badge/Database-PostgreSQL%2016-336791?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?logo=docker&logoColor=white)](https://www.docker.com/)

> **"Connecting Textile Businesses. Simplifying Production."**

---

## 1. Executive Overview

**TexSetu** is an enterprise-grade, cloud-deployable B2B web application specifically engineered to organize, digitize, and coordinate outsourced textile job-work manufacturing in **Surat, Gujarat, India**—the synthetic textile capital of South Asia.

Unlike generic B2C shopping carts or passive lead-generation directories (e.g., IndiaMART), TexSetu is designed around the exact operational realities of decentralized fabric processing:
1. **Capability-Based Provider Discovery**: Sourcing verified dyeing mills, rotary printing units, and garmenting units across GIDC industrial clusters (Pandesara, Sachin, Udhna).
2. **Standardized RFQ & Multi-Quote Comparison**: Structured side-by-side bidding matrices with unit pricing, turnaround lead times, and shrinkage tolerances.
3. **Atomic Concurrency Protection**: Optimistic locking (`@Version`) preventing double-acceptance of quotes and race conditions.
4. **Digital Production Lot Passport**: An auditable, event-driven relational custody ledger linking raw fabric inward counts, physical shrinkage reconciliation, timestamped production stages, and inspection sign-offs.
5. **Gated Quality Inspection & Rework Loop**: Formal quality audits (`ACCEPTED`, `REWORK_REQUIRED`, `REJECTED`) where rework requests automatically revert order states to `PROCESSING` with defect notes recorded in the passport ledger.
6. **Simulated Escrow Payment Rails**: Risk-mitigated commercial settlement protecting both buyers and suppliers.

---

## 2. Technical Stack

| Tier | Technologies Employed |
|:---|:---|
| **Backend Framework** | Java 21 LTS, Spring Boot 3.3.5 |
| **Security & Auth** | Spring Security 6, BCrypt (cost factor 12), CSRF protection, Role-Based Access Control (RBAC) |
| **Data & Persistence**| Hibernate 6, Spring Data JPA, Flyway 10 schema migrations |
| **Databases** | **Dev/Test**: In-Memory H2 (PostgreSQL Compatibility Mode) with auto-seeded demo data.<br>**Production**: PostgreSQL 16 (Multi-AZ with connection pooling via HikariCP). |
| **Presentation Tier** | Server-Side Thymeleaf 3, Bootstrap 5.3, Bootstrap Icons, Custom Industrial Navy & Teal Design System |
| **Automated Testing** | JUnit 5, Mockito, AssertJ, JaCoCo Code Coverage (35 automated tests passing) |
| **DevOps & Cloud** | Multi-stage Dockerfile (Alpine, non-root execution), Docker Compose, GitHub Actions CI |

---

## 3. Quickstart & Local Setup (Zero Dependencies Required)

TexSetu is architected for immediate zero-friction evaluation. In development mode (`dev` profile, active by default), the application boots with an in-memory database pre-populated with realistic Surat market entities, requiring **no local PostgreSQL installation**.

### Prerequisites
- JDK 21 or higher installed on PATH.
- (Maven wrapper `mvnw.cmd` / `mvnw` is included in the project root).

### Running the Application

```bash
# 1. Clone or navigate to the repository
cd d:\TexSetu

# 2. Build and run all automated test suites
.\mvnw.cmd test

# 3. Boot the Spring Boot application
.\mvnw.cmd spring-boot:run
```

Once started, open your web browser to:  
👉 **`http://localhost:8080/`**

---

## 4. Pre-Seeded Demo Accounts & Credentials

The application provides quick-fill demo buttons directly on the login page (`/login`) for instantaneous persona switching:

| Role | Email Address | Password | Persona & Context |
|:---|:---|:---|:---|
| **Textile Buyer** | `buyer@texsetu.com` | `Buyer@123` | Rajeshbhai Shah (Radha Silk Mills, STM Market) |
| **Job-Worker (Mill)**| `worker@texsetu.com` | `Worker@123` | Vikram Patel (Shiv Dyeing & Printing, Pandesara) |
| **System Admin** | `admin@texsetu.com` | `Admin@123` | Platform Administrator & Verifier |

---

## 5. Architectural Highlights & Key Differentiators

### A. The Digital Production Lot Passport
Every confirmed job order automatically initializes a unique production lot (`LOT-YYYYMMDD-XXXX`). The passport manages:
- **Material Inward Reconciliation**: Compares physical fabric roll meters received against the paper delivery challan, establishing an objective baseline before chemical processing.
- **Milestone Event Ledger**: Timestamped, operator-attributed production stage logs (`Scouring`, `Dyeing`, `Stentering`, `Finishing`).
- **Visual Stepper Timeline**: An interactive timeline in the buyer and job-worker views rendering the complete chain of custody.

### B. Concurrency-Safe Bidding Engine
In competitive trading markets, concurrent quote acceptance is prevented via Spring Boot transactions and JPA `@Version` columns on `Rfq` and `JobOrder`. Accepting a quote atomically:
1. Marks the winning quote as `ACCEPTED`.
2. Transitions the RFQ status to `CLOSED`.
3. Auto-rejects all competing quotes as `REJECTED`.
4. Initializes the `JobOrder` and `ProductionLot`.
Any concurrent attempt on an already closed RFQ immediately throws an `IllegalStateException` / `OptimisticLockException`.

### C. State-Guarded Inspection & Rework Loop
The order lifecycle strictly prohibits illegal state jumps (enforced via `OrderStatus.canTransitionTo`). When finished goods arrive, the buyer performs a structured quality audit:
- If marked `ACCEPTED`: The order completes and triggers escrow payment release.
- If marked `REWORK_REQUIRED`: The order **automatically reverts to `PROCESSING`**, with the defect observation permanently appended to the Lot Passport timeline.

---

## 6. Docker & Production Deployment

To run TexSetu using Docker and Docker Compose (PostgreSQL 16 + Spring Boot App):

```bash
# 1. Start application and PostgreSQL containers
docker compose up -d --build

# 2. Monitor startup logs
docker compose logs -f app

# 3. Verify health endpoint
curl http://localhost:8080/actuator/health
```

---

## 7. Project Documentation Directory

Comprehensive architectural, business, and quality documentation is organized under `docs/`:

```
docs/
├── 01-project-overview.md              # High-level architecture and domain mapping
├── 02-problem-statement.md             # Surat cluster pain points and problem hypotheses
├── 03-market-research.md               # Surat MMF cluster metrics, TAM/SAM/SOM sizing
├── 04-competitor-analysis.md           # Feature matrix vs IndiaMART, Zetwerk, WhatsApp
├── 05-topic-proposal.md                # Academic capstone specification & scope
├── 06-lean-canvas.md                   # 9-box Lean Canvas business architecture
├── 07-business-model-canvas.md         # BMC, value architecture, and unit economics
├── 08-swot-analysis.md                 # Strategic SWOT & TOWS matrix
├── 09-pestel-analysis.md               # Macro-environmental PESTEL analysis
├── 10-mvp-scope.md                     # Release boundaries and strict MVP rules
├── pitch-deck.md                       # 12-slide Investor & Academic presentation
├── demo-script.md                      # 5-minute live demonstration script
├── architecture/
│   ├── system-architecture.md          # Monolithic layers, ADRs, security boundaries
│   ├── database-design.md              # 17 normalized tables, data dictionary, indexes
│   ├── uml-class-diagram.md            # Mermaid domain class models and invariants
│   ├── entity-relationship-diagram.md  # Mermaid physical ER diagram
│   └── cloud-architecture.md           # Topography, Docker, VPC, and storage drivers
├── ecommerce/
│   ├── business-model.md               # Take-rates, SaaS tiers, marketplace defense
│   ├── transaction-workflows.md        # Mermaid sequence diagram of 5 transaction phases
│   ├── payment-approach.md             # Escrow simulation and nodal bank roadmap
│   ├── handover-delivery.md            # Physical transport, bulk tempo logistics, shrinkage
│   └── security-compliance.md          # BCrypt, RBAC, CSRF, DPDP Act, and GST compliance
├── testing/
│   ├── test-plan.md                    # Strategy, entry/exit criteria, automation pyramid
│   ├── test-cases.csv                  # 35+ documented test cases with expected results
│   ├── defect-log.csv                  # Resolved defects and engineering remediations
│   ├── regression-report.md            # Test execution results (35/35 passing, 5.01s)
│   ├── performance-test.md             # Benchmark plan, SLOs, and HikariCP pool tuning
│   ├── usability-test-template.md      # UAT feedback templates and scenario scripts
│   └── test-summary.md                 # Quality assurance milestones and sign-off
└── cloud/
    ├── deployment-guide.md             # Zero-downtime production deployment guide
    ├── docker-guide.md                 # Hardened multi-stage Dockerfile architecture
    ├── security.md                     # WAF, DDoS mitigation, container isolation
    ├── monitoring.md                   # Actuator telemetry, Prometheus, SLF4J audit logs
    ├── backup-recovery.md              # Continuous WAL archiving, PITR, disaster recovery
    └── cost-estimate.md                # Cloud cost estimates for 100, 1K, 10K users
```

---

## 8. Academic Evaluation & Defense Checklist

- [x] **Fully Functional Software**: Real, interactive Spring Boot web application (no mockups, no stubs).
- [x] **Relational Integrity**: 17 normalized tables with constraints, indexes, and Flyway versioning.
- [x] **Optimistic Concurrency Control**: Verified quote acceptance locking preventing race conditions.
- [x] **State Machine Validation**: Gated order transitions rejecting illegal lifecycle jumps.
- [x] **Production Lot Passport**: Auditable milestone event ledger tracking physical material reconciliation.
- [x] **Automated Test Coverage**: 35 unit and integration tests passing with 0 failures (`mvnw.cmd test`).
- [x] **Cloud & Containerization**: Multi-stage Dockerfile, Docker Compose, and CI workflow configured.
- [x] **Academic Documentation**: 25+ markdown files, ER diagrams, UML class diagrams, and 12-slide Pitch Deck.
- [x] **Live Demonstration Script**: 5-minute timed script with pre-seeded demo accounts.

---

## 9. License

This project is licensed under the MIT License — see the `LICENSE` file for details.
