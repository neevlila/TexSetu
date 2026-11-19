# Backup & Disaster Recovery (DR) Plan — TexSetu

## 1. Business Continuity Objectives

In B2B manufacturing, data loss directly endangers commercial contracts, challan receipts, and settlement monies. TexSetu defines strict Recovery Point and Recovery Time objectives:

- **Recovery Point Objective (RPO)**: **< 5 Minutes** (Maximum tolerable data loss window in the event of catastrophic primary datacenter outage).
- **Recovery Time Objective (RTO)**: **< 1 Hour** (Maximum acceptable duration to restore full marketplace operations).

---

## 2. Database Backup Topology

```
[Primary PostgreSQL 16 (AWS Mumbai ap-south-1)]
           │
           ├─► Continuous Write-Ahead Log (WAL) Archiving ──► S3 Glacier (Cross-Region)
           │   (Sub-second continuous replay for Point-in-Time Recovery)
           │
           ├─► Automated Daily Full Snapshot (02:00 AM IST) ──► Retained for 30 Days
           │
           └─► Asynchronous Read Replica (AWS Hyderabad ap-south-2)
               (Instant warm standby failover capability)
```

### 2.1 Automated Point-in-Time Recovery (PITR)
- Managed PostgreSQL continuous WAL archiving enables restoration to any specific minute within the preceding 30 calendar days.
- In the event of an accidental batch corruption or catastrophic hardware failure, the database can be replayed to the exact second prior to the incident.

### 2.2 Cold Storage & Compliance Backups
- Encrypted monthly snapshot dumps are exported to AWS S3 Glacier Deep Archive with a statutory 7-year retention policy to satisfy Indian Goods and Services Tax (GST) audit mandates.

---

## 3. Disaster Recovery Failover Protocol

### Step 1: Outage Detection & Triage (T + 0 to 5 mins)
- Automated health monitoring detects failure of primary database or application container cluster.
- Incident Commander confirms outage is unrecoverable within primary Availability Zone.

### Step 2: Standby Database Promotion (T + 5 to 15 mins)
```bash
# Command executed to promote read replica in secondary region
aws rds promote-read-replica --db-instance-identifier texsetu-db-standby-hyderabad
```

### Step 3: Application Container Redirection (T + 15 to 30 mins)
- Update Kubernetes / Docker container database connection string to point to the promoted standby endpoint.
- Start application containers in secondary region.

### Step 4: DNS Switchover & Traffic Restoration (T + 30 to 45 mins)
- Update Cloudflare edge DNS routing: redirect `app.texsetu.com` traffic from Primary IP to Secondary Load Balancer.
- Perform sanity test: verify login, order status view, and Lot Passport timeline rendering.
- Publish status advisory on public status page (`status.texsetu.com`).
