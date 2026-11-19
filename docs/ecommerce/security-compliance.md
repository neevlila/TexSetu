# Security Architecture & Regulatory Compliance — TexSetu

## 1. Security Principles & Defense-in-Depth

TexSetu handles sensitive commercial data, proprietary fabric formulations, pricing quotations, and financial transaction records. The security architecture enforces defense-in-depth across the presentation, application, and persistence layers.

---

## 2. Authentication & Authorization Controls

### 2.1 Password Hashing & Storage
- **Algorithm**: Standardized on BCrypt with a cost factor (strength) of 12 (`BCryptPasswordEncoder`).
- **Plaintext Prohibition**: Passwords are never stored, logged, or serialized.
- **Salt Generation**: BCrypt automatically incorporates a cryptographically secure 128-bit random salt per hash.

### 2.2 Role-Based Access Control (RBAC)
Strict role separation is enforced at both the HTTP security filter chain and service method layers:
- `ROLE_BUYER`: Permitted to create RFQs, view quotations for own RFQs, accept quotes, view own orders, perform quality inspections, and submit reviews.
- `ROLE_JOB_WORKER`: Permitted to manage machine capabilities, view open category RFQs, submit quotes, inward material, update production lot stages, and manage own profile.
- `ROLE_ADMIN`: Permitted to approve/reject worker verification requests, review platform disputes, manage system categories, view system analytics, and export operational CSV reports.

### 2.3 Horizontal & Vertical Privilege Escalation Defense
- **Ownership Verification**: All entity modification requests explicitly query the authenticated `Principal` user ID against the entity’s owning ID (`rfq.getBuyer().getId().equals(currentUser.getId())`).
- **Immutability of System Roles**: User roles (`role` column) are strictly assigned by the system upon registration and cannot be modified via HTTP parameters.

### 2.4 CSRF & Session Security
- **Cross-Site Request Forgery (CSRF)**: Enforced across all state-mutating HTTP methods (`POST`, `PUT`, `DELETE`) via Spring Security’s CSRF token repository. Every Thymeleaf HTML form automatically includes the hidden `_csrf` parameter.
- **Session Cookie Flags**:
  - `HttpOnly = true`: Prevents client-side JavaScript access to session tokens, mitigating XSS session hijacking.
  - `SameSite = Lax`: Prevents cross-site cookie transmission on third-party links.
  - `Secure = true`: Enforces HTTPS-only transmission in production.

---

## 3. Data Protection & Regulatory Compliance

### 3.1 Digital Personal Data Protection (DPDP) Act, 2023 (India)
- **Purpose Limitation**: User contact numbers and business GSTINs are gathered strictly for transactional matching and verification.
- **Data Minimization**: Passwords, banking IFSC codes, and GST numbers are validated with strict regex patterns before persistence.
- **Right to Erasure / Account Deactivation**: Soft-delete mechanisms allow users to suspend or close accounts without compromising historical tax compliance records.

### 3.2 Indian Goods & Services Tax (GST) & Tax Compliance
- **15-Digit GSTIN Validation**: Validated via strict regex format: `^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$`.
- **E-Invoicing Readiness**: All completed order records capture both buyer and job-worker GSTINs, state codes, and HSN/SAC codes (9988 for textile job-work services), facilitating seamless monthly GSTR-1 and GSTR-3B filings.

### 3.3 File Upload Security
- **Size Limitation**: Hard maximum limit of 10 MB per file enforced at the web container and service layer.
- **Extension Whitelist**: Restricted to safe document and image formats: `.pdf`, `.jpg`, `.jpeg`, `.png`. Executable formats (`.exe`, `.sh`, `.bat`, `.js`) are strictly rejected.
- **Filename Sanitization**: Uploaded files are renamed using randomly generated UUIDs (`UUID.randomUUID().toString() + extension`), preventing directory traversal (`../`) attacks.
