# TexSetu B2B Textile Job-Work Marketplace Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a complete, production-grade Spring Boot 3.3+ / Java 21 B2B textile job-work marketplace for Surat textile businesses featuring provider discovery, RFQ workflow, quotation comparison, digital production lot passports, quality inspection, payment simulation, and admin dashboards.

**Architecture:** Layered modular monolith with Presentation (Spring MVC + Thymeleaf + Bootstrap 5), Application (Business Services + Concurrency Locks + Async Notifications), Domain (17 JPA Entities + Enums), and Infrastructure (PostgreSQL/H2, Local/Supabase Storage, Mock Payment Gateway).

**Tech Stack:** Java 21 LTS, Spring Boot 3.3.5, Spring Security 6, Spring Data JPA, Hibernate, PostgreSQL, Flyway, Thymeleaf, Bootstrap 5.3, JUnit 5, Mockito, JaCoCo, Maven.

**Spec:** Defined in Master Project Build Prompt and `TEXSETU_ARCHITECTURE_AND_PLAN.md`.

---

## Global Constraints
- Primary market: Surat, Gujarat, India.
- Roles: `BUYER`, `JOB_WORKER`, `ADMIN`.
- Strict atomic single-quote acceptance per RFQ using optimistic concurrency.
- Production lot passport with immutable audit timeline of events.
- No plain-text passwords; BCrypt hashing throughout.
- Zero external paid dependency blocker for local runs: auto-fallback between PostgreSQL and H2 (Postgres mode).

---

## Review Focus
1. Concurrent quotation acceptance attempt on same RFQ must fail gracefully without race conditions.
2. Job order status transition must follow strict order state machine (no illegal skips).
3. Production event timeline entries must record previous and new status with actor and timestamp.
4. Quality inspection rework must revert status back to PROCESSING with defect reasons.
5. Buyer/Worker authorization boundaries must prevent IDOR on quotes, RFQs, and orders.

---

### Task 1: Project Scaffolding, Maven Configuration & Wrapper
- Create `pom.xml` with dependencies (Web, Security, Data JPA, Validation, Flyway, Thymeleaf, Lombok/records, H2, PostgreSQL, Test).
- Create `mvnw.cmd` and `mvnw` wrapper scripts.
- Configure `.gitignore`, `.env.example`, and Spring Boot properties (`application.yml`, `application-dev.yml`, `application-prod.yml`).

### Task 2: Database Schema & Flyway Migrations
- Create `V1__init_schema.sql` (17 normalized tables, constraints, indexes).
- Create `V2__seed_demo_data.sql` (Surat demo data, categories, sample profiles).

### Task 3: Core Domain Entities & Repositories
- Implement Enums: `Role`, `AccountStatus`, `VerificationStatus`, `UnitOfMeasure`, `RfqStatus`, `QuotationStatus`, `OrderStatus`, `InspectionResult`, `PaymentStatus`, `ComplaintStatus`.
- Implement JPA Entities: `User`, `BusinessProfile`, `JobWorkerProfile`, `ServiceCategory`, `ServiceCapability`, `Rfq`, `RfqAttachment`, `Quotation`, `JobOrder`, `ProductionLot`, `ProductionEvent`, `QualityInspection`, `PaymentRecord`, `Review`, `Complaint`, `Notification`, `VerificationRecord`.
- Implement Spring Data JPA Repositories for all entities.

### Task 4: Core Services & Concurrency Logic
- `UserService` & `CustomUserDetailsService` with BCrypt.
- `JobWorkerService` & `VerificationService`.
- `CatalogService` & `ProviderDiscoveryService` with filtering.
- `RfqQuotationService` with `@Transactional` atomic quotation acceptance.
- `JobOrderService` with strict state machine validation.
- `ProductionLotService` with event auditing.
- `QualityInspectionService` & rework workflow.
- `MockPaymentGateway` & `FileStorageService`.
- `AsyncNotificationService` & `CsvReportService`.

### Task 5: Security & MVC Controllers
- `SecurityConfig` with role-based routing and custom authentication handlers.
- `GlobalExceptionHandler` with user-friendly error views.
- Public Controllers: `HomeController`, `DiscoveryController`, `AuthController`.
- Buyer Controllers: `BuyerDashboardController`, `RfqController`, `BuyerOrderController`.
- Job-Worker Controllers: `JobWorkerDashboardController`, `CatalogManagementController`, `QuotationController`, `ProductionLotController`.
- Admin Controllers: `AdminDashboardController`, `AdminVerificationController`, `AdminComplaintController`.

### Task 6: UI Templates & B2B Design System
- Layout fragments (header, navbar, sidebar, footer, alert banners).
- Public landing, about, browse providers, provider details, login, registration.
- Buyer dashboard, RFQ forms, quote comparison matrix, order & lot tracking view.
- Job-Worker dashboard, quote submission modal/page, production stage update modal.
- Admin dashboard, verification approval queues, complaint resolution, CSV exports.

### Task 7: Automated Testing & JaCoCo
- Unit tests for services, state machine transitions, and concurrency protection.
- Integration tests for auth and order workflows.
- Document 35+ test cases.

### Task 8: DevOps & Comprehensive Documentation
- Multi-stage Dockerfile and docker-compose.yml.
- GitHub Actions CI workflow.
- All required markdown documentation in `docs/`.
- 12-slide Pitch Deck and 5-minute Demo Script.
