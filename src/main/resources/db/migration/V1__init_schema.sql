-- ==============================================================================
-- TEXSETU - B2B TEXTILE JOB-WORK & MANUFACTURING MARKETPLACE
-- V1__init_schema.sql - Initial Normalized Database Schema
-- Compatible with PostgreSQL (Supabase) and H2 (PostgreSQL Mode)
-- ==============================================================================

-- 1. USERS TABLE
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    role VARCHAR(30) NOT NULL, -- BUYER, JOB_WORKER, ADMIN
    account_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, SUSPENDED, DEACTIVATED
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. BUSINESS PROFILES TABLE (Associated with Buyer or Job-Worker)
CREATE TABLE business_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    business_name VARCHAR(150) NOT NULL,
    business_type VARCHAR(50) NOT NULL, -- TRADER, MANUFACTURER, CLOTHING_BRAND, JOB_WORK_UNIT
    gst_number VARCHAR(25),
    pan_number VARCHAR(20),
    address TEXT NOT NULL,
    city VARCHAR(80) NOT NULL,
    state VARCHAR(80) NOT NULL DEFAULT 'Gujarat',
    pincode VARCHAR(10) NOT NULL,
    contact_person VARCHAR(100) NOT NULL,
    contact_phone VARCHAR(20) NOT NULL,
    contact_email VARCHAR(150) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 3. JOB WORKER PROFILES TABLE (Specialized capabilities & operational metrics)
CREATE TABLE job_worker_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    business_name VARCHAR(150) NOT NULL,
    business_type VARCHAR(80) NOT NULL,
    description TEXT,
    city VARCHAR(80) NOT NULL DEFAULT 'Surat',
    service_area VARCHAR(150) NOT NULL DEFAULT 'Surat & South Gujarat',
    address TEXT NOT NULL,
    machinery_details TEXT,
    indicative_capacity_meters_per_day NUMERIC(12, 2) NOT NULL DEFAULT 0.0,
    turnaround_days INT NOT NULL DEFAULT 3,
    verification_status VARCHAR(30) NOT NULL DEFAULT 'PENDING', -- PENDING, VERIFIED, REJECTED, SUSPENDED
    verification_notes TEXT,
    capacity_last_updated TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    verified_at TIMESTAMP WITHOUT TIME ZONE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 4. SERVICE CATEGORIES TABLE
CREATE TABLE service_categories (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 5. SERVICE CAPABILITIES TABLE (Job worker catalog offerings)
CREATE TABLE service_capabilities (
    id BIGSERIAL PRIMARY KEY,
    job_worker_id BIGINT NOT NULL REFERENCES job_worker_profiles(id) ON DELETE CASCADE,
    category_id BIGINT NOT NULL REFERENCES service_categories(id) ON DELETE RESTRICT,
    service_name VARCHAR(150) NOT NULL,
    description TEXT,
    minimum_quantity NUMERIC(12, 2) NOT NULL,
    unit_of_measure VARCHAR(20) NOT NULL, -- METERS, KILOGRAMS, PIECES, BATCHES
    indicative_price_per_unit NUMERIC(12, 2) NOT NULL,
    lead_time_days INT NOT NULL DEFAULT 3,
    processing_details TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 6. RFQS (REQUEST FOR QUOTATION)
CREATE TABLE rfqs (
    id BIGSERIAL PRIMARY KEY,
    rfq_number VARCHAR(40) NOT NULL UNIQUE,
    buyer_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id BIGINT NOT NULL REFERENCES service_categories(id) ON DELETE RESTRICT,
    title VARCHAR(200) NOT NULL,
    fabric_description TEXT NOT NULL,
    quantity NUMERIC(12, 2) NOT NULL,
    unit_of_measure VARCHAR(20) NOT NULL,
    desired_completion_date DATE NOT NULL,
    processing_requirements TEXT NOT NULL,
    technical_specifications TEXT,
    delivery_requirements TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN', -- DRAFT, OPEN, QUOTED, CLOSED, CANCELLED
    version BIGINT NOT NULL DEFAULT 0, -- Optimistic locking for atomic quote acceptance
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 7. RFQ ATTACHMENTS
CREATE TABLE rfq_attachments (
    id BIGSERIAL PRIMARY KEY,
    rfq_id BIGINT NOT NULL REFERENCES rfqs(id) ON DELETE CASCADE,
    file_name VARCHAR(255) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    file_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    uploaded_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 8. QUOTATIONS TABLE
CREATE TABLE quotations (
    id BIGSERIAL PRIMARY KEY,
    quote_number VARCHAR(40) NOT NULL UNIQUE,
    rfq_id BIGINT NOT NULL REFERENCES rfqs(id) ON DELETE CASCADE,
    job_worker_id BIGINT NOT NULL REFERENCES job_worker_profiles(id) ON DELETE CASCADE,
    unit_price NUMERIC(12, 2) NOT NULL,
    total_price NUMERIC(14, 2) NOT NULL,
    min_quantity_conditions NUMERIC(12, 2),
    estimated_duration_days INT NOT NULL,
    proposed_completion_date DATE NOT NULL,
    terms_conditions TEXT,
    validity_date DATE NOT NULL,
    additional_notes TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'SUBMITTED', -- SUBMITTED, ACCEPTED, REJECTED, WITHDRAWN, EXPIRED
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_positive_quote_price CHECK (unit_price > 0 AND total_price > 0)
);

-- 9. JOB ORDERS TABLE
CREATE TABLE job_orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(40) NOT NULL UNIQUE,
    quotation_id BIGINT NOT NULL UNIQUE REFERENCES quotations(id) ON DELETE RESTRICT,
    rfq_id BIGINT NOT NULL REFERENCES rfqs(id) ON DELETE RESTRICT,
    buyer_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    job_worker_id BIGINT NOT NULL REFERENCES job_worker_profiles(id) ON DELETE RESTRICT,
    category_id BIGINT NOT NULL REFERENCES service_categories(id) ON DELETE RESTRICT,
    agreed_quantity NUMERIC(12, 2) NOT NULL,
    unit_of_measure VARCHAR(20) NOT NULL,
    agreed_price NUMERIC(14, 2) NOT NULL,
    expected_completion_date DATE NOT NULL,
    current_status VARCHAR(30) NOT NULL DEFAULT 'CONFIRMED', 
    -- States: CONFIRMED, MATERIAL_RECEIVED, PROCESSING, QUALITY_CHECK, COMPLETED, DISPATCHED, DELIVERED, CANCELLED
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 10. DIGITAL PRODUCTION LOT PASSPORTS TABLE
CREATE TABLE production_lots (
    id BIGSERIAL PRIMARY KEY,
    lot_number VARCHAR(40) NOT NULL UNIQUE,
    order_id BIGINT NOT NULL UNIQUE REFERENCES job_orders(id) ON DELETE CASCADE,
    original_submitted_quantity NUMERIC(12, 2) NOT NULL,
    received_quantity NUMERIC(12, 2) NOT NULL,
    processing_category VARCHAR(100) NOT NULL,
    processing_notes TEXT,
    current_stage VARCHAR(50) NOT NULL DEFAULT 'LOT_INITIALIZED',
    quantity_completed NUMERIC(12, 2) NOT NULL DEFAULT 0.0,
    quantity_rejected_waste NUMERIC(12, 2) NOT NULL DEFAULT 0.0,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, ON_HOLD, REWORK, CLOSED
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 11. PRODUCTION EVENTS TABLE (Auditable event ledger)
CREATE TABLE production_events (
    id BIGSERIAL PRIMARY KEY,
    lot_id BIGINT NOT NULL REFERENCES production_lots(id) ON DELETE CASCADE,
    event_type VARCHAR(60) NOT NULL,
    previous_state VARCHAR(50),
    new_state VARCHAR(50) NOT NULL,
    actor_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    actor_role VARCHAR(30) NOT NULL,
    description TEXT NOT NULL,
    attachment_url VARCHAR(500),
    event_timestamp TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 12. QUALITY INSPECTIONS TABLE
CREATE TABLE quality_inspections (
    id BIGSERIAL PRIMARY KEY,
    lot_id BIGINT NOT NULL REFERENCES production_lots(id) ON DELETE CASCADE,
    order_id BIGINT NOT NULL REFERENCES job_orders(id) ON DELETE CASCADE,
    inspector_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    inspector_role VARCHAR(30) NOT NULL,
    inspection_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    quantity_inspected NUMERIC(12, 2) NOT NULL,
    accepted_quantity NUMERIC(12, 2) NOT NULL,
    rejected_quantity NUMERIC(12, 2) NOT NULL DEFAULT 0.0,
    defect_details TEXT,
    inspection_comments TEXT NOT NULL,
    result VARCHAR(30) NOT NULL, -- ACCEPTED, REWORK_REQUIRED, REJECTED
    evidence_photo_url VARCHAR(500),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 13. PAYMENT RECORDS (SIMULATION)
CREATE TABLE payment_records (
    id BIGSERIAL PRIMARY KEY,
    transaction_reference VARCHAR(50) NOT NULL UNIQUE,
    order_id BIGINT NOT NULL REFERENCES job_orders(id) ON DELETE CASCADE,
    payer_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    payee_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    amount NUMERIC(14, 2) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'INR',
    payment_method VARCHAR(40) NOT NULL DEFAULT 'SIMULATED_ESCROW',
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING', -- INITIATED, SUCCESS, FAILED, PENDING, REFUND_REQUESTED, REFUNDED
    gateway_response TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 14. REVIEWS TABLE (Transaction-linked)
CREATE TABLE reviews (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE REFERENCES job_orders(id) ON DELETE CASCADE,
    reviewer_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    reviewee_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 15. COMPLAINTS TABLE
CREATE TABLE complaints (
    id BIGSERIAL PRIMARY KEY,
    complaint_reference VARCHAR(50) NOT NULL UNIQUE,
    order_id BIGINT NOT NULL REFERENCES job_orders(id) ON DELETE CASCADE,
    complainant_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    category VARCHAR(50) NOT NULL, -- QUALITY_DEFECT, DELAY, SHORTAGE, BILLING, OTHER
    description TEXT NOT NULL,
    evidence_url VARCHAR(500),
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN', -- OPEN, UNDER_REVIEW, RESOLVED, REJECTED
    admin_notes TEXT,
    resolution_notes TEXT,
    resolved_at TIMESTAMP WITHOUT TIME ZONE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 16. NOTIFICATIONS TABLE
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    target_url VARCHAR(255),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 17. VERIFICATION RECORDS TABLE (Admin verification audit trail)
CREATE TABLE verification_records (
    id BIGSERIAL PRIMARY KEY,
    job_worker_id BIGINT NOT NULL REFERENCES job_worker_profiles(id) ON DELETE CASCADE,
    admin_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    status VARCHAR(30) NOT NULL, -- PENDING, VERIFIED, REJECTED, SUSPENDED
    review_notes TEXT,
    submitted_docs TEXT,
    reviewed_at TIMESTAMP WITHOUT TIME ZONE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- INDEXES FOR PERFORMANCE & LOOKUPS
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_job_worker_city ON job_worker_profiles(city);
CREATE INDEX idx_job_worker_status ON job_worker_profiles(verification_status);
CREATE INDEX idx_service_cap_job_worker ON service_capabilities(job_worker_id);
CREATE INDEX idx_service_cap_cat ON service_capabilities(category_id);
CREATE INDEX idx_rfqs_buyer ON rfqs(buyer_id);
CREATE INDEX idx_rfqs_status ON rfqs(status);
CREATE INDEX idx_rfqs_category ON rfqs(category_id);
CREATE INDEX idx_quotations_rfq ON quotations(rfq_id);
CREATE INDEX idx_quotations_worker ON quotations(job_worker_id);
CREATE INDEX idx_job_orders_buyer ON job_orders(buyer_id);
CREATE INDEX idx_job_orders_worker ON job_orders(job_worker_id);
CREATE INDEX idx_job_orders_status ON job_orders(current_status);
CREATE INDEX idx_prod_events_lot ON production_events(lot_id);
CREATE INDEX idx_notifications_user ON notifications(user_id, is_read);
