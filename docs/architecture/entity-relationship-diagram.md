# Entity-Relationship (ER) Diagram — TexSetu

## 1. Complete Physical Schema Diagram

The physical database schema contains 17 tables normalized to Third Normal Form (3NF), maintaining strict referential integrity through foreign key cascades and constraints.

```mermaid
erDiagram
    USERS ||--o| BUSINESS_PROFILES : "has"
    USERS ||--o| JOB_WORKER_PROFILES : "has"
    JOB_WORKER_PROFILES ||--o{ SERVICE_CAPABILITIES : "offers"
    SERVICE_CATEGORIES ||--o{ SERVICE_CAPABILITIES : "groups"

    USERS ||--o{ RFQS : "creates as buyer"
    SERVICE_CATEGORIES ||--o{ RFQS : "specifies"
    RFQS ||--o{ RFQ_ATTACHMENTS : "includes"
    RFQS ||--o{ QUOTATIONS : "receives"
    USERS ||--o{ QUOTATIONS : "submits as worker"

    RFQS ||--o| JOB_ORDERS : "crystallizes into"
    QUOTATIONS ||--o| JOB_ORDERS : "binds"
    USERS ||--o{ JOB_ORDERS : "buyer in"
    USERS ||--o{ JOB_ORDERS : "worker in"

    JOB_ORDERS ||--|| PRODUCTION_LOTS : "tracks via"
    PRODUCTION_LOTS ||--o{ PRODUCTION_EVENTS : "logs"

    JOB_ORDERS ||--o{ QUALITY_INSPECTIONS : "evaluated by"
    JOB_ORDERS ||--o{ PAYMENT_RECORDS : "settled through"
    JOB_ORDERS ||--o{ REVIEWS : "reviewed in"
    JOB_ORDERS ||--o{ COMPLAINTS : "disputed via"

    USERS ||--o{ NOTIFICATIONS : "receives"
    JOB_WORKER_PROFILES ||--o{ VERIFICATION_RECORDS : "verified by"

    USERS {
        bigint id PK
        varchar email UK
        varchar password
        varchar role
        varchar status
        varchar full_name
        timestamp created_at
    }

    BUSINESS_PROFILES {
        bigint id PK
        bigint user_id FK, UK
        varchar company_name
        varchar gstin
        varchar city
        varchar state
    }

    JOB_WORKER_PROFILES {
        bigint id PK
        bigint user_id FK, UK
        varchar factory_address
        varchar industrial_area
        decimal daily_capacity
        varchar verification_status
    }

    RFQS {
        bigint id PK
        varchar rfq_number UK
        bigint buyer_id FK
        bigint category_id FK
        decimal quantity
        varchar unit
        varchar status
        int version
    }

    QUOTATIONS {
        bigint id PK
        bigint rfq_id FK
        bigint job_worker_id FK
        decimal unit_price
        decimal total_amount
        varchar status
    }

    JOB_ORDERS {
        bigint id PK
        varchar order_number UK
        bigint rfq_id FK
        bigint quotation_id FK
        bigint buyer_id FK
        bigint job_worker_id FK
        varchar status
        decimal total_amount
        int version
    }

    PRODUCTION_LOTS {
        bigint id PK
        varchar lot_number UK
        bigint order_id FK, UK
        decimal challan_quantity
        decimal actual_quantity_received
        varchar current_stage
        boolean is_reconciled
    }

    PRODUCTION_EVENTS {
        bigint id PK
        bigint lot_id FK
        varchar stage_name
        text event_description
        timestamp created_at
    }
```

---

## 2. Integrity and Referential Guarantees

1. **Lot Passport Uniqueness**: Each confirmed `JOB_ORDER` possesses exactly one `PRODUCTION_LOT` record (`order_id` carries a `UNIQUE` constraint).
2. **Immutable Audit Trailing**: `PRODUCTION_EVENTS` records can only be inserted; application services expose no update or delete endpoints for milestone history.
3. **Atomic Quote Acceptance**: The foreign key `JOB_ORDERS.quotation_id` guarantees that every work order points to the explicitly selected quotation.
