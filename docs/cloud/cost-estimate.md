# Cloud Infrastructure Cost Estimation — TexSetu

## 1. Cost Modeling Assumptions & Tiering

Because TexSetu is built as an efficient Spring Boot 3 modular monolith without microservice proliferation, its infrastructure overhead is remarkably lean. Cost projections are modeled across three distinct scale tiers:

1. **Pilot / Early MVP Scale**: 100 active monthly users (Surat pilot: 20 mills, 80 traders).
2. **Cluster Commercial Traction**: 1,000 active monthly users (Surat cluster saturation: 150 mills, 850 traders).
3. **Statewide Regional Scale**: 10,000 active monthly users (Expansion across Surat, Ahmedabad, and South Gujarat).

---

## 2. Infrastructure Cost Breakdown by Scale Tier

### Tier 1: Pilot / Early MVP (100 Active Users)
*Architecture: Single Docker Container on Hetzner / AWS Lightsail + Managed DB*

| Component | Specification | Monthly Cost (USD) | Monthly Cost (INR @ ₹84) |
|:---|:---|:---:|:---:|
| Application Container Host | AWS Lightsail / EC2 (2 vCPU, 4 GB RAM) | $20.00 | ₹1,680 |
| Managed PostgreSQL DB | AWS RDS db.t4g.micro (2 vCPU, 1 GB RAM, 20 GB SSD) | $15.00 | ₹1,260 |
| Object Storage (Attachments) | AWS S3 Standard (10 GB storage + egress) | $1.50 | ₹126 |
| Edge Security & DNS | Cloudflare Free Tier | $0.00 | ₹0 |
| Transactional SMS & Email | Fast2SMS / SendGrid (2,000 notifications) | $10.00 | ₹840 |
| Domain & SSL | Namecheap FQDN + Automated Let's Encrypt | $1.20 | ₹100 |
| **TOTAL TIER 1 MONTHLY COST** | | **$47.70** | **₹4,006** |

---

### Tier 2: Cluster Commercial Traction (1,000 Active Users)
*Architecture: High-Availability Dual Container + Managed Multi-AZ RDS*

| Component | Specification | Monthly Cost (USD) | Monthly Cost (INR @ ₹84) |
|:---|:---|:---:|:---:|
| Application Cluster | 2x AWS EC2 t4g.medium (2 vCPU, 4 GB RAM each) | $60.00 | ₹5,040 |
| Application Load Balancer | AWS Application Load Balancer (ALB) | $22.00 | ₹1,848 |
| Managed PostgreSQL DB | AWS RDS db.t4g.small Multi-AZ (2 vCPU, 2 GB RAM, 50 GB SSD)| $68.00 | ₹5,712 |
| Object Storage (Attachments) | AWS S3 Standard (100 GB storage + 200 GB transfer) | $12.00 | ₹1,008 |
| Edge Security & WAF | Cloudflare Pro ($20/mo) | $20.00 | ₹1,680 |
| Notifications & SMS Gateway | Gupshup / Exotel (25,000 notifications) | $75.00 | ₹6,300 |
| Monitoring & Log Storage | Grafana Cloud / AWS CloudWatch | $15.00 | ₹1,260 |
| **TOTAL TIER 2 MONTHLY COST** | | **$272.00** | **₹22,848** |

---

### Tier 3: Statewide Regional Scale (10,000 Active Users)
*Architecture: Auto-Scaling Kubernetes (EKS) Cluster + High-Throughput Aurora PostgreSQL*

| Component | Specification | Monthly Cost (USD) | Monthly Cost (INR @ ₹84) |
|:---|:---|:---:|:---:|
| Container Compute Cluster | AWS EKS (4x c7g.large instances with Auto-Scaling) | $320.00 | ₹26,880 |
| Load Balancing & Networking | AWS ALB + NAT Gateway Data Processing | $75.00 | ₹6,300 |
| Enterprise Database | Amazon Aurora PostgreSQL (db.r6g.large Multi-AZ) | $340.00 | ₹28,560 |
| Object Storage (Attachments) | AWS S3 Standard (1 TB storage + CDN egress) | $85.00 | ₹7,140 |
| Edge Security & Enterprise WAF| Cloudflare Business Tier | $200.00 | ₹16,800 |
| SMS, WhatsApp Business API | Meta WhatsApp API + SMS Gateway (250,000 alerts) | $650.00 | ₹54,600 |
| Enterprise Observability | Datadog / Grafana Enterprise Managed | $120.00 | ₹10,080 |
| **TOTAL TIER 3 MONTHLY COST** | | **$1,790.00** | **₹1,50,360** |

---

## 3. Revenue-to-Cost Margin Analysis (Unit Economics at Scale)

At Tier 2 (1,000 active users processing 4,000 orders monthly with an average order take-rate of ₹1,250):
- **Monthly Gross Platform Revenue**: $4,000 \times ₹1,250 = ₹50,00,000$ (~$59,500 USD).
- **Monthly Cloud Infrastructure Cost**: ₹22,848 (~$272 USD).
- **Cloud Infrastructure Cost Ratio**: **0.46% of Gross Revenue**!
- This extraordinary margin efficiency demonstrates the immense architectural advantage of a streamlined Java 21 / Spring Boot modular monolith over bloated microservice architectures.
