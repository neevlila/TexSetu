# Cloud Monitoring & Observability Architecture — TexSetu

## 1. Observability Stack Overview

TexSetu implements comprehensive telemetry across the three pillars of observability: **Metrics**, **Logs**, and **Health Checks**.

```
┌────────────────────────────────────────────────────────────────────────┐
│                        TEXSETU OBSERVABILITY PIPELINE                  │
├───────────────────┬────────────────────┬───────────────────────────────┤
│ 1. HEALTH PROBES  │ 2. METRICS         │ 3. STRUCTURED LOGS            │
│                   │                    │                               │
│ • /actuator/health│ • Prometheus /     │ • SLF4J + Logback             │
│ • Liveness probe  │   Micrometer       │ • JSON structured format      │
│ • Readiness probe │ • JVM Heap & GC    │ • Correlation IDs (MDC)       │
│ • DB ping test    │ • HikariCP pool    │ • Log aggregation (Grafana    │
│                   │ • HTTP latency p95 │   Loki / CloudWatch)          │
└───────────────────┴────────────────────┴───────────────────────────────┘
```

---

## 2. Spring Boot Actuator Endpoints

The application exposes monitored operational endpoints via Spring Boot Actuator under `/actuator`:
- **`/actuator/health`**: Real-time component health (database connectivity, disk space).
- **`/actuator/metrics`**: Detailed runtime performance telemetry (HTTP request count, response time histograms, active thread count).
- **`/actuator/info`**: Application version, build timestamp, and Git commit hash.

In production, these endpoints are secured and accessible only from internal cluster monitoring networks.

---

## 3. Key Monitoring Alerts & Thresholds

| Metric | Warning Threshold | Critical Threshold | Automated Action |
|:---|:---:|:---:|:---|
| **JVM Heap Memory Utilization** | > 75% | > 90% | Trigger heap dump & alert on-call engineer |
| **Database Connection Pool Exhaustion** | > 80% active | > 95% active | Scale pool size & log thread trace |
| **HTTP 5xx Server Error Rate** | > 1.0% | > 3.0% | PagerDuty alert; automated container restart |
| **Disk Space on Upload Volume** | > 80% full | > 90% full | Trigger log rotation & archive to S3 |
| **Failed Login Attempts** | > 10 / min | > 30 / min | Cloudflare IP rate-limit block |

---

## 4. Log Management & Audit Tracing

All transactional operations in TexSetu (Quotation acceptance, Inward reconciliation, Quality inspection) emit structured log statements:
```json
{
  "timestamp": "2026-11-19T14:32:15.120Z",
  "level": "INFO",
  "logger": "com.texsetu.service.RfqQuotationService",
  "message": "Quotation accepted atomically",
  "rfqNumber": "RFQ-20261119-7412",
  "quotationId": 482,
  "orderNumber": "ORD-20261119-9182",
  "acceptedByUserId": 14,
  "executionTimeMs": 28
}
```
Logs are shipped via Promtail to Grafana Loki or AWS CloudWatch Logs with a 90-day retention policy for dispute auditing.
