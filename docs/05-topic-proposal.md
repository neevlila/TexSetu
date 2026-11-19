# 05. Topic Proposal & Academic Capstone Specification — TexSetu

## 1. Project Title & Metadata
- **Project Name**: TexSetu (B2B Textile Job-Work and Manufacturing Marketplace)
- **Tagline**: "Connecting Textile Businesses. Simplifying Production."
- **Domain**: Cloud-Based B2B E-Commerce & Supply Chain Coordination Platform
- **Target Geographic Hub**: Surat & South Gujarat Textile Manufacturing Cluster
- **Academic Focus**: Distributed Systems Architecture, Event-Driven Audit Ledgers, Concurrency Control in B2B Marketplaces, Role-Based Access Control (RBAC), and Transaction Management.

---

## 2. Problem Statement & Academic Motivation
Decentralized textile manufacturing hubs such as Surat, India depend heavily on horizontal specialization where fabric production is divided across hundreds of independent MSME job-workers (dyeing, printing, embroidery, garmenting). Current business coordination relies predominantly on manual phone calls, unformatted WhatsApp messages, and vulnerable paper delivery challans.

This project addresses three fundamental software engineering and product design challenges:
1. **Concurrency and Double-Acceptance in Competitive B2B Bidding**: Ensuring that when multiple providers quote against a single Request for Quotation (RFQ), accepting a quote atomically closes the RFQ and automatically rejects competing offers without race conditions.
2. **Auditable Material Reconciliation (The Production Lot Passport)**: Implementing an event-sourced audit ledger that logs material inward counts, stage progression, physical shrinkage percentages, and inspection sign-offs with cryptographic and database-level integrity.
3. **Multi-Tenant State Machine Integrity**: Enforcing a strict, unidirectional workflow transition model (from Draft to Completed/Disputed) where unauthorized state jumps (e.g., jumping from Order Placed to Completed without stage progression) are strictly rejected at the service layer.

---

## 3. System Architecture & Technical Stack
- **Backend Framework**: Spring Boot 3.3.5 running on Java 21 LTS.
- **Security & Authorization**: Spring Security 6 with BCrypt password hashing, session-based CSRF protection, and strict role hierarchy (`ROLE_BUYER`, `ROLE_JOB_WORKER`, `ROLE_ADMIN`).
- **Database & Persistence**:
  - Development & In-Memory Testing: H2 Database in PostgreSQL Compatibility Mode.
  - Production Environment: PostgreSQL 16 on Cloud / Supabase.
  - Schema Migration: Flyway 10 with versioned migration scripts.
- **Frontend Presentation**: Server-side rendered Thymeleaf 3 templates integrated with Bootstrap 5.3, Bootstrap Icons, and custom Industrial Navy & Teal CSS design tokens.
- **Testing & Quality Assurance**: JUnit 5, Mockito, AssertJ, and JaCoCo Code Coverage (target: >80% on core domain logic).

---

## 4. Key Functional Modules to be Evaluated

### Module A: Authentication & Verification
- Secure multi-persona registration and login for Buyers and Job-Workers.
- Administrative verification lifecycle: `PENDING`, `VERIFIED`, `REJECTED`, `SUSPENDED` with audit remarks.

### Module B: Capability-Based Discovery & Search
- Full-text search and faceted filtering by category, geographic cluster (Pandesara, Sachin, Udhna), minimum order quantity (MOQ), and indicative turnaround times.

### Module C: RFQ & Atomic Quotation Comparison
- Buyer RFQ creation with fabric technical specifications and target turnaround.
- Provider quotation submission with price per unit, estimated completion date, and validity period.
- Multi-quote comparison matrix and transactional acceptance with optimistic locking (`@Version`).

### Module D: Digital Production Lot Passport
- Inward material receipt recording: physical units received vs. challan specification.
- Production milestone ledger: timestamped stage updates with notes and document references.
- Visual stepper timeline rendering the complete lifecycle from dispatch to final delivery.

### Module E: Quality Inspection & Rework Loop
- Formal buyer quality audit with three deterministic outcomes: `ACCEPTED`, `REWORK_REQUIRED`, and `REJECTED`.
- Rework loop that automatically reverts the job order to `PROCESSING` with defect notes, re-engaging the job-worker.

### Module F: Administrative Governance & Audit Reporting
- Admin dashboard displaying platform GMV, active dispute queue, and user verifications.
- Streaming CSV export for operational reporting (Job Orders, Provider Profiles, Production Lots).

---

## 5. Scope Boundaries (Strict MVP Rules)
To ensure academic rigor and a fully functional deliverable, the following intentional boundaries are maintained:
1. **Three Core Processing Categories**: Focus exclusively on (1) Dyeing & Processing, (2) Printing & Embroidery, and (3) Garment Stitching.
2. **Single-Leg Work Orders**: Each job order connects one buyer with one primary job-worker for a designated processing service (multi-factory supply chain graphs are reserved for future work).
3. **Simulated Escrow Gateway**: Financial transactions are processed through an interactive mock escrow gateway that demonstrates ledger states without requiring PCI-DSS payment compliance.
4. **Zero Pseudo-Claims**: The Digital Production Lot Passport is explicitly documented as a relational, auditable transaction ledger and does not claim to use blockchain or automated computer-vision defect scanning.
