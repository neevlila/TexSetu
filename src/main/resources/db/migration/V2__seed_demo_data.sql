-- ==============================================================================
-- TEXSETU - B2B TEXTILE JOB-WORK & MANUFACTURING MARKETPLACE
-- V2__seed_demo_data.sql - Realistic Demo Seed Data (Surat Textile Hub)
-- All accounts have default demo password: Password123! (except Admin: Admin@TexSetu2026!)
-- BCrypt for Password123! : $2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.
-- ==============================================================================

-- 1. SEED SERVICE CATEGORIES (MVP 3 Core Categories)
INSERT INTO service_categories (id, code, name, description, active) VALUES
(1, 'DYE_FABRIC_PROC', 'Dyeing and Fabric Processing', 'Continuous dyeing, soft flow jet dyeing, mercerization, bleaching, and chemical finishing.', TRUE),
(2, 'PRINT_EMBROIDERY', 'Printing and Embroidery', 'High-speed rotary screen printing, reactive digital textile printing, and multi-head computerized embroidery.', TRUE),
(3, 'STITCH_FINISHING', 'Garment Stitching and Finishing', 'Industrial sewing, flatlock/overlock stitching, button-holing, industrial steam pressing, and export packing.', TRUE);

-- 2. SEED USERS
-- Password for all is Password123!
-- BCrypt: $2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.
INSERT INTO users (id, email, password_hash, full_name, phone_number, role, account_status) VALUES
(1, 'admin@texsetu.com', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.', 'TexSetu Master Admin', '+91 98250 11000', 'ADMIN', 'ACTIVE'),
(2, 'radhe.dyeing@texsetu.demo', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.', 'Rameshchandra Patel', '+91 98251 22334', 'JOB_WORKER', 'ACTIVE'),
(3, 'surat.digital@texsetu.demo', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.', 'Ketanbhai Shah', '+91 98252 33445', 'JOB_WORKER', 'ACTIVE'),
(4, 'omkar.garments@texsetu.demo', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.', 'Jignesh Mewada', '+91 98253 44556', 'JOB_WORKER', 'ACTIVE'),
(5, 'ambica.unverified@texsetu.demo', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.', 'Pravinbhai Solanki', '+91 98254 55667', 'JOB_WORKER', 'ACTIVE'),
(6, 'meera.fashions@texsetu.demo', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.', 'Meera Singhania', '+91 98790 66778', 'BUYER', 'ACTIVE'),
(7, 'vandana.fabrics@texsetu.demo', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.', 'Sureshchandra Agarwal', '+91 98791 77889', 'BUYER', 'ACTIVE');

-- 3. SEED BUSINESS PROFILES
INSERT INTO business_profiles (user_id, business_name, business_type, gst_number, panNumber, address, city, state, pincode, contact_person, contact_phone, contact_email) VALUES
(2, 'Radhe Krishna Dyeing & Processing Mills', 'JOB_WORK_UNIT', '24AAACR1234F1Z1', 'AAACR1234F', 'Plot No. 42-45, Phase 2, Pandesara GIDC', 'Surat', 'Gujarat', '394221', 'Ramesh Patel', '+91 98251 22334', 'radhe.dyeing@texsetu.demo'),
(3, 'Surat Digital Prints & Laser Embroidery', 'JOB_WORK_UNIT', '24AABCS5678G1Z2', 'AABCS5678G', 'Shed 12B, Road No. 6, Sachin GIDC', 'Surat', 'Gujarat', '394230', 'Ketan Shah', '+91 98252 33445', 'surat.digital@texsetu.demo'),
(4, 'Omkar Garments & Finishing Unit', 'JOB_WORK_UNIT', '24AADCO9012H1Z3', 'AADCO9012H', '2nd Floor, Anjana Farm Industrial Estate, Ring Road', 'Surat', 'Gujarat', '395002', 'Jignesh Mewada', '+91 98253 44556', 'omkar.garments@texsetu.demo'),
(5, 'Shree Ambica Textile Jobworks', 'JOB_WORK_UNIT', '24AAECS3456J1Z4', 'AAECS3456J', 'Survey 88, Near Bhestan Railway Crossing, Udhna', 'Surat', 'Gujarat', '394210', 'Pravin Solanki', '+91 98254 55667', 'ambica.unverified@texsetu.demo'),
(6, 'Meera Fashion House', 'CLOTHING_BRAND', '24AAFCM7890K1Z5', 'AAFCM7890K', 'Shop 412, Surat Textile Market (STM), Ring Road', 'Surat', 'Gujarat', '395002', 'Meera Singhania', '+91 98790 66778', 'meera.fashions@texsetu.demo'),
(7, 'Vandana Fabrics & Wholesale Traders', 'TRADER', '24AAGCV1234L1Z6', 'AAGCV1234L', 'Lower Ground, Millennium Textile Market, Ring Road', 'Surat', 'Gujarat', '395002', 'Suresh Agarwal', '+91 98791 77889', 'vandana.fabrics@texsetu.demo');

-- 4. SEED JOB WORKER PROFILES
INSERT INTO job_worker_profiles (id, user_id, business_name, business_type, description, city, service_area, address, machinery_details, indicative_capacity_meters_per_day, turnaround_days, verification_status, verification_notes, verified_at) VALUES
(1, 2, 'Radhe Krishna Dyeing & Processing Mills', 'Dyeing & Processing Mill', 'Leading cotton, modal, and rayon dyeing unit with multi-ton soft flow jet dyeing and stenter machines.', 'Surat', 'Pandesara & South Gujarat', 'Plot 42-45, Phase 2, Pandesara GIDC', '4x Soft Flow Jet Dyeing (Sclavos), 2x 8-Chamber Stenters (Monforts), Continuous Bleaching Range', 25000.0, 4, 'VERIFIED', 'Physical GIDC unit and pollution control board certificates inspected.', CURRENT_TIMESTAMP),
(2, 3, 'Surat Digital Prints & Laser Embroidery', 'Digital Printing & Embroidery Hub', 'Ultra high-definition direct-to-fabric reactive printing and computerized multi-head Schiffli/Tajima embroidery.', 'Surat', 'Sachin GIDC & Surat City', 'Shed 12B, Road No. 6, Sachin GIDC', '3x Kyocera High-Speed Digital Printers (1200 dpi), 4x 32-Head Tajima Computerized Embroidery Machines', 8000.0, 3, 'VERIFIED', 'Registered GST verified, machinery ownership verified.', CURRENT_TIMESTAMP),
(3, 4, 'Omkar Garments & Finishing Unit', 'Apparel Stitching & Finishing Unit', 'Contract apparel manufacturing specialized in women kurtis, western wear, shirts, and industrial finish pressing.', 'Surat', 'Ring Road & Surat Textile Hub', '2nd Floor, Anjana Farm Industrial Estate, Ring Road', '60x Juki Lockstitch DDL-9000C, 15x Yamato Overlock Machines, Vacuum Steam Ironing Table', 2000.0, 5, 'VERIFIED', 'Factory premises and worker safety compliance verified.', CURRENT_TIMESTAMP),
(4, 5, 'Shree Ambica Textile Jobworks', 'Small Scale Embroidery Job-Work', 'Independent job-work unit catering to local boutique and wholesale saree embroidery orders.', 'Surat', 'Udhna & Bhestan', 'Survey 88, Near Bhestan Railway Crossing, Udhna', '2x 12-Head Multi-needle Embroidery Machines', 800.0, 6, 'PENDING', 'Awaiting municipal trade license submission.', NULL);

-- 5. SEED SERVICE CAPABILITIES
INSERT INTO service_capabilities (job_worker_id, category_id, service_name, description, minimum_quantity, unit_of_measure, indicative_price_per_unit, lead_time_days, processing_details, active) VALUES
(1, 1, 'Reactive Jet Dyeing (Rayon / Viscose)', 'Precision shade matching with high colorfastness reactive dyes on high-grade viscose and rayon fabrics.', 1000.0, 'METERS', 14.50, 4, 'Includes desizing, scouring, bleaching, dyeing, and stenter heat-setting.', TRUE),
(1, 1, 'Cotton Mercerized Fabric Processing', 'Caustic soda mercerization and silicone soft finish for premium luster and tensile strength.', 1500.0, 'METERS', 18.00, 5, 'Shrinkage controlled within 2.5%, shade delta E < 0.6.', TRUE),
(2, 2, 'High-Definition Reactive Digital Printing', 'Direct reactive digital printing on pure cotton, silk, and viscose up to 64-inch width.', 500.0, 'METERS', 48.00, 3, 'Sub-millimeter color alignment, steaming, washing, and loop drying included.', TRUE),
(2, 2, 'Multi-Head Multi-Color Zari Embroidery', 'High density multi-color thread and metallic zari embroidery for kurtis, dupattas, and sarees.', 300.0, 'METERS', 65.00, 4, 'Computerized punch design conversion and backing paper stripping included.', TRUE),
(3, 3, 'Complete Kurti Stitching with Interlock', 'Complete cutting, stitching, neck piping, overlocking, side slits, and hem folding for standard ladies kurtis.', 200.0, 'PIECES', 85.00, 4, 'Graded pattern cutting, premium poly-core thread, 100% inner overlock.', TRUE),
(3, 3, 'Garment Steam Ironing & Poly-Packing', 'Industrial boiler steam pressing, tag attachment, folding with butter paper, and individual polybag sealing.', 500.0, 'PIECES', 12.00, 2, 'Vacuum suction pressing, barcode sticker application.', TRUE);

-- 6. SEED SAMPLE RFQ
INSERT INTO rfqs (id, rfq_number, buyer_id, category_id, title, fabric_description, quantity, unit_of_measure, desired_completion_date, processing_requirements, technical_specifications, delivery_requirements, status, version) VALUES
(1, 'RFQ-SRT-2026-001', 6, 2, 'Reactive Digital Printing for Rayon Saree Collection', '100% Liva-certified Rayon Viscose 30x30 / 68x64, 44-inch grey fabric supplied.', 2500.0, 'METERS', CURRENT_DATE + INTERVAL '10' DAY, 'Need vibrant botanical floral print across 5 different colorways. High washing fastness (grade 4+).', 'Reactive dye inks only. Steaming at 102 C for 12 minutes followed by thorough hot/cold wash.', 'Deliver rolled and plastic wrapped on heavy cardboard tubes to STM Shop 412.', 'QUOTED', 0),
(2, 'RFQ-SRT-2026-002', 7, 3, 'Bulk Stitching & Finishing of Anarkali Flared Kurtis', 'Stitched from printed cotton cambric fabric (supplied cut or roll). 3-panel flared design.', 1200.0, 'PIECES', CURRENT_DATE + INTERVAL '15' DAY, 'Neckline lace attachment, side dori with tassels, 5-thread overlock inside.', 'Sizes: S(200), M(400), L(400), XL(200). Size tags and brand woven labels to be stitched.', 'Individual polybag packing in master cartons of 50 pieces.', 'OPEN', 0);

-- 7. SEED SAMPLE QUOTATIONS
INSERT INTO quotations (id, quote_number, rfq_id, job_worker_id, unit_price, total_price, min_quantity_conditions, estimated_duration_days, proposed_completion_date, terms_conditions, validity_date, additional_notes, status) VALUES
(1, 'QUO-2026-001', 1, 2, 45.00, 112500.00, 2000.0, 4, CURRENT_DATE + INTERVAL '6' DAY, 'Payment 30% advance on material receipt, balance upon quality approval. Waste tolerance +/- 2%.', CURRENT_DATE + INTERVAL '5' DAY, 'We have running Kyocera machines and can start lot immediately upon grey roll delivery.', 'SUBMITTED');
