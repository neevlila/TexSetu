# 10. MVP Scope & Release Boundaries — TexSetu

## 1. Scope Discipline & Boundary Principles

To build a reliable, high-integrity product ready for university demonstration and commercial launch, TexSetu adheres to strict release boundaries. Features that introduce unnecessary complexity, require physical IoT infrastructure, or distract from the core B2B job-work transaction loop are systematically excluded from the MVP.

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                                 THE TEXSETU MVP BOUNDARY                               │
├───────────────────────────────────────────────────────┬────────────────────────────────┤
│ IN-SCOPE (Phase 1 MVP Release)                        │ OUT-OF-SCOPE (Future Roadmap)  │
├───────────────────────────────────────────────────────┼────────────────────────────────┤
│ • 3 Core Processing Categories:                       │ • Unlimited textile processes  │
│   1. Dyeing & Fabric Processing                       │   (weaving, spinning, sizing,  │
│   2. Printing & Embroidery                            │   yarn texturing, knitting).   │
│   3. Garment Stitching & Finishing                    │ • Multi-tier supply chain      │
│ • Single-Leg Work Orders (1 Buyer -> 1 Job-Worker).   │   orchestration (5-stage graph)│
│ • Standard Textile Units (Meters, Kgs, Pieces, Batches)│ • Real-time IoT machine sensors│
│ • Optimistic Locking Quotation Acceptance.            │ • Automated AI production scheduling│
│ • Digital Production Lot Passport (Relational Ledger).│ • Blockchain / Smart Contracts │
│ • Material Inward Count vs Challan Reconciliation.    │ • Native in-house trucking fleet│
│ • Gated Inspection & Rework Loop.                     │ • Physical chemical lab audits │
│ • Simulated Escrow Payment Gateway.                   │ • Production payment gateway   │
│ • Role-Based Dashboards (Buyer, Job-Worker, Admin).   │   (Razorpay/Stripe production) │
│ • Streaming CSV Operational Export.                   │ • Mobile Native Apps (iOS/Android)│
└───────────────────────────────────────────────────────┴────────────────────────────────┘
```

---

## 2. In-Scope Module Specifications

### Module 1: User & Authentication Management
- Three primary roles: `BUYER`, `JOB_WORKER`, `ADMIN`.
- Account verification lifecycle for job-workers (`PENDING`, `VERIFIED`, `REJECTED`, `SUSPENDED`).
- Session-based CSRF protection, BCrypt password hashing, and role-based redirect routing.

### Module 2: Job-Worker Profile & Capability Directory
- Business profiles with GSTIN, PAN, business type, contact information, and factory address.
- Capability catalog specifying service category, machinery types, daily capacity, minimum order quantity (MOQ), and indicative pricing per unit.
- Real-time faceted discovery allowing buyers to filter by category, city/cluster, MOQ, and turnaround time.

### Module 3: Request for Quotation (RFQ) Workflow
- Structured RFQ creation with fabric specifications (e.g., Viscose Georgette, 60 GSM), quantity, unit of measurement, target completion date, and delivery notes.
- Provider quotation submission with price per unit, turnaround days, quotation validity date, and notes.
- Comparison matrix presenting all quotes side-by-side with lowest-price and fastest-delivery highlights.
- Concurrency-safe single-quote acceptance: atomically transitions RFQ to `CLOSED`, marks accepted quote as `ACCEPTED`, rejects competing quotes as `REJECTED`, and spawns the `JobOrder` and `ProductionLot`.

### Module 4: Digital Production Lot Passport & Tracking
- Dynamic generation of unique Lot Identifier (`LOT-YYYYMMDD-XXXX`).
- Inward material receipt logging: tracks challan quantity, actual physical quantity received, and inward remarks.
- Immutable production event log: records timestamps, stage names (e.g., `Material Inwarding`, `Dyeing/Processing`, `Quality Inspection`, `Ready for Dispatch`), user identity, and notes.
- Visual stepper timeline rendering the complete custody trail.

### Module 5: Quality Inspection, Rework, and Settlement
- Structured buyer inspection submission: rating, formal outcome (`ACCEPTED`, `REWORK_REQUIRED`, `REJECTED`), and defect observations.
- Rework loop: if marked `REWORK_REQUIRED`, the order automatically reverts to `PROCESSING` with defect notes recorded in the passport ledger.
- Settlement & Handover: upon acceptance, order transitions to `COMPLETED`, triggering simulated escrow fund release and enabling buyer reviews.

### Module 6: Administration & Operational Reporting
- Centralized verification review for admin approval or rejection of job-workers.
- Marketplace dispute and complaint review.
- High-efficiency streaming CSV exports for Orders, Providers, and Production Lots.

---

## 3. Explicit Product Disclaimers & Integrity Constraints

1. **No Claims of Blockchain**: The Digital Production Lot Passport is implemented as a relational, transaction-consistent database ledger (`production_lots` and `production_events` with foreign keys and unique constraints). It is never referred to as a "decentralized blockchain".
2. **Provider-Entered Capacity**: Job-worker capacity is self-reported and explicitly displayed with a "Last Updated" timestamp. The platform does not claim real-time IoT integration with factory machinery.
3. **Evidence, Not Absolute Guarantee**: Document and photo attachments represent evidence of what was submitted by the user or provider; they do not guarantee the absence of physical fabric defects.
