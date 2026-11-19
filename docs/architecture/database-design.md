# Database Design & Schema Specification — TexSetu

## 1. Relational Database Strategy

TexSetu employs a fully normalized relational schema designed for PostgreSQL 16 (and H2 in PostgreSQL mode for development and testing). The schema comprises **17 relational tables** with comprehensive primary keys, foreign key constraints, check constraints, unique indexes, and optimistic locking versions.

All DDL definitions are versioned via Flyway under `src/main/resources/db/migration/V1__init_schema.sql`.

---

## 2. Complete Entity-Relationship Overview

```
                                    ┌───────────────┐
                                    │     USERS     │
                                    └───┬───────┬───┘
                                        │ 1     │ 1
                 ┌──────────────────────┘       └──────────────────────┐
                 │ 1                                                   │ 1
        ┌────────┴────────┐                                   ┌────────┴────────┐
        │BUSINESS_PROFILES│                                   │JOB_WORKER_PROFILES│
        └─────────────────┘                                   └───┬─────────────┘
                                                                  │ 1
                                                                  │ N
                                                        ┌─────────┴─────────────┐
                                                        │  SERVICE_CAPABILITIES │
                                                        └─────────┬─────────────┘
                                                                  │ N
                                                                  │ 1
                                                        ┌─────────┴─────────────┐
                                                        │  SERVICE_CATEGORIES   │
                                                        └───────────────────────┘

                 ┌───────────────┐
                 │     RFQS      │◄────────────────┐ 1
                 └───┬───────┬───┘                 │
                     │ 1     │ 1                   │
                     │ N     │ N                   │
        ┌────────────┴──┐   ┌┴───────────┐         │
        │RFQ_ATTACHMENTS│   │ QUOTATIONS │         │
        └───────────────┘   └───┬────────┘         │
                                │ 1                │
                                │ 1                │
                            ┌───┴────────┐         │
                            │ JOB_ORDERS │─────────┘
                            └───┬────────┘
                                │ 1
                                │ 1
                          ┌─────┴──────────┐
                          │PRODUCTION_LOTS │
                          └─────┬──────────┘
                                │ 1
                                │ N
                          ┌─────┴──────────┐
                          │PRODUCTION_EVENTS│
                          └────────────────┘
```

---

## 3. Detailed Data Dictionary

### Table 1: `users`
Stores user authentication credentials, system roles, and account statuses.
| Column | Type | Constraints | Description |
|:---|:---|:---|:---|
| `id` | `BIGSERIAL` | `PRIMARY KEY` | Unique user identifier |
| `email` | `VARCHAR(150)` | `NOT NULL, UNIQUE` | User login email |
| `password` | `VARCHAR(255)` | `NOT NULL` | BCrypt encrypted password hash |
| `role` | `VARCHAR(30)` | `NOT NULL` | Enum: `BUYER`, `JOB_WORKER`, `ADMIN` |
| `status` | `VARCHAR(30)` | `NOT NULL` | Enum: `PENDING`, `VERIFIED`, `REJECTED`, `SUSPENDED` |
| `full_name` | `VARCHAR(100)` | `NOT NULL` | Full name of the user |
| `phone_number` | `VARCHAR(20)` | `NULLABLE` | Primary contact phone |
| `created_at` | `TIMESTAMP` | `NOT NULL` | Record creation timestamp |
| `updated_at` | `TIMESTAMP` | `NOT NULL` | Record last updated timestamp |

### Table 2: `business_profiles`
Stores commercial identity, tax numbers, and primary operating address.
| Column | Type | Constraints | Description |
|:---|:---|:---|:---|
| `id` | `BIGSERIAL` | `PRIMARY KEY` | Profile identifier |
| `user_id` | `BIGINT` | `NOT NULL, UNIQUE, FK -> users(id)` | Associated user |
| `company_name` | `VARCHAR(150)` | `NOT NULL` | Registered trading name |
| `business_type` | `VARCHAR(50)` | `NOT NULL` | Proprietorship, Partnership, Pvt Ltd |
| `gstin` | `VARCHAR(15)` | `NULLABLE` | 15-character Indian Goods & Services Tax ID |
| `pan_number` | `VARCHAR(10)` | `NULLABLE` | Permanent Account Number |
| `address_line1`| `VARCHAR(255)` | `NOT NULL` | Street / Market shop address |
| `city` | `VARCHAR(50)` | `NOT NULL` | City (e.g., Surat) |
| `state` | `VARCHAR(50)` | `NOT NULL` | State (e.g., Gujarat) |
| `pincode` | `VARCHAR(10)` | `NOT NULL` | Postal PIN code |

### Table 3: `job_worker_profiles`
Stores textile job-worker operational capacity, machinery, and verification state.
| Column | Type | Constraints | Description |
|:---|:---|:---|:---|
| `id` | `BIGSERIAL` | `PRIMARY KEY` | Job-worker profile identifier |
| `user_id` | `BIGINT` | `NOT NULL, UNIQUE, FK -> users(id)` | Associated job-worker account |
| `factory_address` | `VARCHAR(255)`| `NOT NULL` | Mill/Factory address in GIDC |
| `industrial_area`| `VARCHAR(100)`| `NOT NULL` | Cluster (Pandesara, Sachin, Udhna) |
| `machinery_details`| `TEXT` | `NULLABLE` | Machine list (Soft flow, Rotary, Stenter) |
| `daily_capacity` | `DECIMAL(12,2)`| `NULLABLE` | Indicative daily processing capacity |
| `capacity_unit` | `VARCHAR(20)` | `NULLABLE` | Meters, Kgs, Pieces |
| `capacity_updated_at`| `TIMESTAMP` | `NULLABLE` | Freshness timestamp for capacity |
| `verification_status`| `VARCHAR(30)` | `NOT NULL` | `PENDING`, `VERIFIED`, `REJECTED` |

### Table 4: `rfqs`
Stores buyer Requests for Quotation.
| Column | Type | Constraints | Description |
|:---|:---|:---|:---|
| `id` | `BIGSERIAL` | `PRIMARY KEY` | RFQ identifier |
| `rfq_number` | `VARCHAR(50)` | `NOT NULL, UNIQUE` | Display reference (`RFQ-YYYYMMDD-XXXX`) |
| `buyer_id` | `BIGINT` | `NOT NULL, FK -> users(id)` | Requesting buyer |
| `category_id` | `BIGINT` | `NOT NULL, FK -> service_categories(id)` | Target service category |
| `title` | `VARCHAR(200)` | `NOT NULL` | RFQ title |
| `fabric_type` | `VARCHAR(100)` | `NOT NULL` | Fabric spec (Viscose Georgette, Cotton) |
| `quantity` | `DECIMAL(12,2)`| `NOT NULL` | Quantity required |
| `unit` | `VARCHAR(20)` | `NOT NULL` | `METERS`, `KGS`, `PIECES`, `BATCHES` |
| `target_completion_date`| `DATE` | `NOT NULL` | Desired delivery deadline |
| `status` | `VARCHAR(30)` | `NOT NULL` | `DRAFT`, `OPEN`, `QUOTED`, `CLOSED`, `CANCELLED` |
| `version` | `INT` | `NOT NULL DEFAULT 0` | Optimistic locking version |

### Table 5: `quotations`
Stores job-worker price and turnaround bids against RFQs.
| Column | Type | Constraints | Description |
|:---|:---|:---|:---|
| `id` | `BIGSERIAL` | `PRIMARY KEY` | Quotation identifier |
| `rfq_id` | `BIGINT` | `NOT NULL, FK -> rfqs(id)` | Quoted RFQ |
| `job_worker_id`| `BIGINT` | `NOT NULL, FK -> users(id)` | Bidding job-worker |
| `unit_price` | `DECIMAL(10,2)`| `NOT NULL` | Price per unit (INR) |
| `total_amount` | `DECIMAL(12,2)`| `NOT NULL` | Calculated total |
| `estimated_turnaround_days`| `INT` | `NOT NULL` | Lead time in days |
| `validity_date` | `DATE` | `NOT NULL` | Expiration date of quotation |
| `status` | `VARCHAR(30)` | `NOT NULL` | `SUBMITTED`, `ACCEPTED`, `REJECTED`, `EXPIRED` |

### Table 6: `job_orders`
Stores confirmed commercial work orders.
| Column | Type | Constraints | Description |
|:---|:---|:---|:---|
| `id` | `BIGSERIAL` | `PRIMARY KEY` | Order identifier |
| `order_number` | `VARCHAR(50)` | `NOT NULL, UNIQUE` | Order reference (`ORD-YYYYMMDD-XXXX`) |
| `rfq_id` | `BIGINT` | `NOT NULL, FK -> rfqs(id)` | Originating RFQ |
| `quotation_id` | `BIGINT` | `NOT NULL, FK -> quotations(id)` | Accepted quotation |
| `buyer_id` | `BIGINT` | `NOT NULL, FK -> users(id)` | Contracted buyer |
| `job_worker_id`| `BIGINT` | `NOT NULL, FK -> users(id)` | Contracted job-worker |
| `status` | `VARCHAR(30)` | `NOT NULL` | `PLACED`, `MATERIAL_INWARDED`, `PROCESSING`, `INSPECTION`, `COMPLETED`, `CANCELLED`, `DISPUTED` |
| `total_amount` | `DECIMAL(12,2)`| `NOT NULL` | Agreed contract value |
| `version` | `INT` | `NOT NULL DEFAULT 0` | Optimistic locking version |

### Table 7: `production_lots` & Table 8: `production_events` (The Lot Passport)
- `production_lots` tracks the physical lot passport: `lot_number`, `order_id`, `challan_quantity`, `actual_quantity_received`, `material_inward_date`, `current_stage`, and `is_reconciled`.
- `production_events` records the immutable timestamped audit history: `lot_id`, `stage_name`, `event_description`, `performed_by_id`, `document_reference`, and `created_at`.

---

## 4. Indexing & Performance Strategy
1. **B-Tree Unique Indexes**: Enforce global uniqueness on `users.email`, `rfqs.rfq_number`, `job_orders.order_number`, and `production_lots.lot_number`.
2. **Foreign Key Composite Indexes**: Created on `(rfq_id, job_worker_id)` in `quotations` to guarantee immediate sub-second query performance during quotation comparison matrix rendering.
3. **Faceted Filtering Indexes**: Created on `job_worker_profiles(industrial_area, verification_status)` and `service_capabilities(category_id, min_order_quantity)` to accelerate buyer discovery searches.
