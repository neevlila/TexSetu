# System Architecture Document — TexSetu

## 1. Architectural Style & Design Principles

TexSetu is architected as a **modular, monolithic Spring Boot 3 enterprise application** following clean domain-driven architectural patterns. The architecture prioritizes strong transactional boundaries, database-level consistency, testability, and operational simplicity.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                             PRESENTATION LAYER                              │
│   Thymeleaf Templates + Bootstrap 5.3 + Custom Industrial UI Theme (CSS)    │
│            Role-Based Navbars, Modal Dialogs, Responsive Tables             │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │ HTTP / Form Submissions / CSRF
┌──────────────────────────────────────▼──────────────────────────────────────┐
│                              CONTROLLER LAYER                               │
│  • AuthController           • BuyerDashboardController                      │
│  • DiscoveryController      • JobWorkerDashboardController                  │
│  • HomeController           • AdminDashboardController                      │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │ DTOs / Entity Invocations
┌──────────────────────────────────────▼──────────────────────────────────────┐
│                               SERVICE LAYER                                 │
│  • UserService              • JobOrderService                               │
│  • JobWorkerService         • ProductionLotService                          │
│  • CatalogService           • QualityInspectionService                      │
│  • RfqQuotationService      • MockPaymentGateway / CsvReportService         │
│  • LocalFileStorageService  • NotificationServiceImpl                       │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │ Spring Data JPA / Transactions
┌──────────────────────────────────────▼──────────────────────────────────────┐
│                             PERSISTENCE LAYER                               │
│      17 Spring Data JPA Repositories (Hibernate 6 / JPA Specifications)     │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │ SQL / Flyway Migrations
┌──────────────────────────────────────▼──────────────────────────────────────┐
│                              DATABASE STORAGE                               │
│   • Development: H2 In-Memory (PostgreSQL Mode) + Seed Data                 │
│   • Production: PostgreSQL 16 Enterprise Relational Database                │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Layer Responsibilities & Boundaries

### 2.1 Presentation Layer
- **Server-Side Rendering (SSR)**: Thymeleaf 3 is utilized to deliver fast, SEO-friendly, zero-client-build web pages without JavaScript bundling overhead.
- **Role-Aware Security Tags**: Utilizes `sec:authorize="hasRole('BUYER')"` to conditionally display navigation controls and action buttons.
- **Design System**: Industrial Navy (`#1B365D`) and Vibrant Teal (`#00A896`) color tokens, with distinct status badges for order and lot lifecycle stages.

### 2.2 Controller Layer
- Responsible solely for HTTP request decoding, input validation (`@Valid`), security context resolution, and view routing.
- Never contains business logic; delegates directly to domain services within explicit transaction boundaries.

### 2.3 Service Layer
- **Strict Transactional Boundaries (`@Transactional`)**: Ensures all multi-table mutations (e.g., accepting a quote, initializing order, generating lot passport, and logging inward events) execute atomically.
- **Optimistic Concurrency Control**: Employs `@Version` fields to protect critical entities (`Rfq`, `JobOrder`) against concurrent race conditions.
- **Domain Event Auditing**: Enforces append-only milestone logging to `production_events` whenever orders progress through lifecycle stages.

### 2.4 Persistence Layer
- 17 Spring Data JPA interfaces extending `JpaRepository` and `JpaSpecificationExecutor`.
- Supports dynamic querying and multi-attribute faceted search across categories, cities, MOQs, and turnaround times.

---

## 3. Key Architectural Decisions (ADRs)

### ADR-01: Modular Monolith vs. Microservices
- **Decision**: Build TexSetu as a cohesive Spring Boot modular monolith rather than a distributed microservices network.
- **Rationale**: B2B job-work transactions require atomic ACID guarantees across RFQs, quotes, orders, and lot records. Distributed transactions (2PC or Sagas) add unnecessary network latency, operational overhead, and failure modes without benefit for this initial scale.
- **Status**: Implemented & Verified.

### ADR-02: Server-Side Thymeleaf vs. Single Page Application (SPA)
- **Decision**: Standardize on Thymeleaf 3 with Bootstrap 5 rather than React/Vue/Node.js.
- **Rationale**: Eliminates cross-origin CORS security risks, duplicate validation logic, API version drift, and complex Node build toolchains. Allows a unified Java 21 codebase with zero external npm dependencies.
- **Status**: Implemented & Verified.

### ADR-03: Optimistic Concurrency on Quotation Acceptance
- **Decision**: Apply JPA `@Version` integer columns on `Rfq` and `JobOrder`.
- **Rationale**: In high-velocity trading markets, a buyer or multi-user purchasing desk might simultaneously click "Accept" on different quotes. Optimistic locking immediately aborts concurrent attempts with an `OptimisticLockException`, preventing double-awarding of contracts.
- **Status**: Implemented & Verified via `RfqQuotationConcurrencyTest`.

### ADR-04: Digital Production Lot Passport Architecture
- **Decision**: Implement the Lot Passport as a normalized parent-child relational model (`production_lots` and `production_events`) with strict immutable event auditing.
- **Rationale**: Provides complete auditability, millisecond query performance, foreign-key referential integrity, and SQL export capabilities without the latency, financial cost, and environmental overhead of blockchain smart contracts.
- **Status**: Implemented & Verified.
