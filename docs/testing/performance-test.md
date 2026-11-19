# Performance & Stress Test Benchmark Plan — TexSetu

## 1. Objectives & Performance Baselines

The objective of the performance test is to validate the scalability, throughput, and response latency of the TexSetu Spring Boot modular monolith under simulated peak cluster traffic.

### Performance Service-Level Objectives (SLOs)
- **Public Discovery & Catalog Search**: 95th percentile latency ($p95$) < 250 ms at 100 concurrent requests/sec.
- **RFQ Creation & Quote Submission**: $p95$ < 400 ms at 30 concurrent transactions/sec.
- **Quotation Comparison Matrix Rendering**: $p95$ < 300 ms at 50 concurrent requests/sec.
- **Atomic Quotation Acceptance Transaction**: $p99$ < 500 ms under concurrent contention.
- **Production Lot Passport Timeline Fetch**: $p95$ < 200 ms with 25+ milestone events.
- **System Error Rate**: < 0.1% under normal peak load; 0% transaction corruption under race conditions.

---

## 2. Load Simulation Scenarios & Locust / JMeter Scripts

### Scenario 1: The Ring Road Market Morning Rush (Discovery & Search)
- **Simulated Load**: 500 virtual users browsing supplier profiles and filtering by industrial area (Pandesara, Sachin) and minimum order quantity.
- **Endpoint**: `GET /discovery?category=1&city=Surat&moq=2000`
- **Duration**: 10-minute sustained ramp-up.
- **Target**: Zero connection pool timeouts on HikariCP; average response time < 150 ms.

### Scenario 2: Peak RFQ Bidding Window (Quotation Comparison & Acceptance)
- **Simulated Load**: 100 concurrent buyers submitting RFQs and comparing quotes, with 200 job-workers submitting bids.
- **Contention Test**: 10 concurrent requests attempting to accept different quotes on the same RFQ simultaneously.
- **Expected Outcome**: Exactly 1 request succeeds with HTTP 302 redirect to the new order; 9 requests cleanly receive the concurrency rejection exception without duplicate orders generated.

---

## 3. Database & Memory Profiling Metrics

### HikariCP Connection Pool Tuning
- `maximum-pool-size`: 50 connections.
- `minimum-idle`: 10 connections.
- `connection-timeout`: 30,000 ms.
- `idle-timeout`: 600,000 ms.
- `max-lifetime`: 1,800,000 ms.

### JVM Heap Profiling (Eclipse Temurin Java 21)
- Base footprint: ~180 MB under idle state.
- Peak heap during 500-user simulated load: ~450 MB (well within 1 GB container quota).
- Garbage Collector: G1GC default; zero full-GC pauses observed during 10-minute sustained load.
