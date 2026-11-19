# Comprehensive Test Plan — TexSetu

## 1. Test Strategy & Quality Philosophy

The TexSetu quality assurance strategy follows the **Test Automation Pyramid**, balancing fast, isolated unit tests, high-fidelity integration test suites, and end-to-end transactional scenario verification.

```
                  /\
                 /  \      Manual Exploratory & UAT
                /----\     (Usability, Mobile Responsive)
               /      \
              /--------\   Spring Boot Integration Tests
             /          \  (WebMvcTest, DataJpaTest, State Machine)
            /------------\
           /              \ Isolated Unit Tests & Mockito
          /----------------\ (Domain logic, Concurrency, Gateways)
```

---

## 2. Test Scope & Categorization

### 2.1 Automated Test Suites in Repository
The test suite resides in `src/test/java/com/texsetu/` and consists of 8 comprehensive test classes executing 35 automated test cases:

1. **`UserServiceTest`**:
   - User registration with password hashing.
   - Duplicate email rejection.
   - Business profile creation and linkage.
   - Job-worker profile verification transitions (`PENDING` -> `VERIFIED` / `REJECTED`).

2. **`RfqQuotationConcurrencyTest`**:
   - RFQ creation with positive quantity and future date validation.
   - Quotation submission by verified job-workers.
   - Atomic quotation acceptance: verifies winning quote is `ACCEPTED`, RFQ transitions to `CLOSED`, competing quotes are marked `REJECTED`, and `JobOrder` is spawned.
   - Optimistic concurrency control verification simulating race conditions.

3. **`JobOrderStateMachineTest`**:
   - Strict validation of legal lifecycle progressions: `PLACED` -> `MATERIAL_INWARDED` -> `PROCESSING` -> `INSPECTION` -> `COMPLETED`.
   - Illegal state jump rejection (e.g., attempting to jump directly from `PLACED` to `COMPLETED`).
   - Order cancellation rules and dispute routing.

4. **`ProductionLotPassportTest`**:
   - Automatic lot passport generation upon order creation with unique ID format (`LOT-YYYYMMDD-XXXX`).
   - Inward material reconciliation: recording challan quantity vs. physical fabric received.
   - Production event logging: timestamped milestone logging (`Scouring`, `Dyeing`, `Stentering`).
   - Verification of ledger immutability and sequential retrieval.

5. **`QualityInspectionAndReworkTest`**:
   - Successful inspection sign-off (`ACCEPTED`) completing the order and unlocking reviews.
   - Defect inspection (`REWORK_REQUIRED`) automatically reverting order to `PROCESSING` with defect notes recorded in the passport.
   - Rejection handling (`REJECTED`) routing order to dispute status.

6. **`MockPaymentGatewayTest`**:
   - Escrow payment initiation: verifying transaction ID and `HELD_IN_ESCROW` status.
   - Escrow payment release: verifying state moves to `RELEASED_TO_WORKER` only after inspection acceptance.
   - Escrow refund processing upon cancellation.

7. **`CsvReportServiceTest`**:
   - Streaming CSV export for Job Orders: verifying header schema and record count.
   - Streaming CSV export for Provider Directory and Production Lots.

8. **`FileStorageAndCatalogTest`**:
   - Local file storage: file saving, size boundary enforcement (10MB limit), and UUID sanitization.
   - Service category catalog retrieval and active status filtering.

---

## 3. Entry and Exit Criteria

### Entry Criteria
- Clean Maven compilation (`mvnw.cmd test-compile` succeeds with zero errors).
- H2 in-memory test database initialized with PostgreSQL compatibility mode.
- Mockito byte-buddy agent dynamic loading enabled.

### Exit Criteria
- **100% Test Pass Rate**: All 35 tests pass without failures or errors (`Failures: 0, Errors: 0, Skipped: 0`).
- **Code Coverage**: Minimum 80% instruction coverage across core service and entity lifecycle methods via JaCoCo.
- **Zero Critical / Blocker Defects**: No unresolved P1/P2 issues in the defect log.
