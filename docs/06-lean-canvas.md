# 06. Lean Canvas — TexSetu

```
┌────────────────────────┬────────────────────────┬────────────────────────┬────────────────────────┬────────────────────────┐
│ 1. PROBLEM             │ 4. SOLUTION            │ 3. UNIQUE VALUE PROP.  │ 9. UNFAIR ADVANTAGE    │ 2. CUSTOMER SEGMENTS   │
│                        │                        │                        │                        │                        │
│ • Fragmented broker-   │ • Capability-filtered  │ "Connecting Textile    │ • First-mover vertical │ • PRIMARY BUYERS:      │
│   driven discovery;    │   job-worker search    │ Businesses. Simplifying│   focus on Surat MMF   │   Textile merchant     │
│   1-3% broker leakage. │   across GIDC clusters.│ Production."           │   cluster workflows.   │   traders in STM &     │
│ • Opaque pricing &     │ • Multi-parameter RFQ  │                        │ • Proprietary Digital  │   Millennium Market.   │
│   unstandardized phone │   quotation comparison │ Digital Production Lot │   Production Lot       │ • Clothing brands and  │
│   quotations.          │   matrix.              │ Passport providing an  │   Passport data model. │   garment wholesalers. │
│ • "Black-box" factory  │ • Digital Production   │ auditable chain-of-    │ • Strategic alignment  │ • JOB-WORKERS:         │
│   production; zero WIP │   Lot Passport for     │ custody from grey      │   with local textile   │   Dyeing & processing  │
│   visibility.          │   material tracking.   │ fabric receipt to      │   trade associations   │   mills (Pandesara/    │
│ • Frequent disputes    │ • Formal inspection &  │ delivery and quality   │   (SGCCI, FOSTTA).     │   Sachin GIDC).        │
│   over shrinkage and   │   rework workflow.     │ sign-off.              │                        │   Printing & embroidery│
│   shade variation; no  │                        │                        │                        │   units (Udhna).       │
│   auditable records.   │                        │                        │                        │   Stitching jobbers.   │
├────────────────────────┼────────────────────────┼────────────────────────┼────────────────────────┼────────────────────────┤
│ EXISTING ALTERNATIVES: │ 8. KEY METRICS         │ HIGH-LEVEL CONCEPT:    │ 5. CHANNELS            │ EARLY ADOPTERS:        │
│                        │                        │                        │                        │                        │
│ • Local brokers        │ • Verified Job-Workers │ "Shopify + Flexport    │ • Direct merchant      │ • Tech-forward second- │
│   (Dalals).            │   onboarded.           │ for decentralized      │   associations (FOSTTA,│   generation textile   │
│ • Phone calls & messy  │ • RFQs posted & quote  │ textile processing     │   SGCCI).              │   traders selling on   │
│   WhatsApp groups.     │   response rate.       │ clusters."             │ • Field sales agents   │   Meesho/Myntra.       │
│ • Paper delivery       │ • Work orders created  │                        │   stationed at Ring    │ • Tier-2 processing    │
│   challans (Kachha).   │   & completed GMV.     │                        │   Road textile markets.│   mills looking to fill│
│ • IndiaMART (generic,  │ • Dispute resolution   │                        │ • Word-of-mouth &      │   idle mid-week soft-  │
│   no transaction).     │   rate & rework cycle. │                        │   digital referrals.   │   flow capacity.       │
├────────────────────────┴────────────────────────┴────────────────────────┴────────────────────────┴────────────────────────┤
│ 7. COST STRUCTURE                               │ 6. REVENUE STREAMS                                                       │
│                                                 │                                                                          │
│ • Cloud hosting & database infrastructure (AWS/PostgreSQL).   │ • Marketplace Transaction Fee: 0.75% to 1.5% on completed orders.│
│ • Engineering, product development & maintenance.             │ • Tiered Job-Worker Subscriptions (Verified Gold Badge, priority)│
│ • Ground operations, onboarding & physical mill verification. │ • Value-Added Services: Premium analytics & batch lab reports.   │
│ • Customer support & dispute mediation team.                  │                                                                          │
└─────────────────────────────────────────────────┴──────────────────────────────────────────────────────────────────────────┘
```

---

## 1. Problem Hypotheses & Validation

### Validated Problem Hypotheses
1. **Quotation Friction**: Sourcing quotes across 4 mills for 10,000 meters of georgette dyeing currently takes 24 to 48 hours of phone calls, with quotes frequently omitting shrinkage tolerances and finishing charges.
2. **Fabric Traceability Deficit**: Inward grey fabric counts are recorded on loose paper slips that are vulnerable to oil stains, water damage, or loss on processing floors.
3. **Settlement Delays**: Disagreements regarding whether defective fabric was caused by weaving imperfections or poor chemical processing result in delayed payments lasting 60–90 days.

### De-scoped Hypotheses (Excluded from MVP)
- Real-time IoT machine sensor telemetry is not economically viable for Tier-2/3 Indian job-workers at present due to retrofitting costs; human-in-the-loop milestone confirmation is the viable mechanism.
- Fully automated algorithmic capacity scheduling cannot account for sudden Surat power grid fluctuations or boiler steam maintenance; self-reported capacity with timestamped freshness is superior.

---

## 2. Unfair Advantages & Barriers to Entry
1. **Verticalized Domain Abstractions**: Generic platforms do not understand "Grey Fabric", "Meters vs. Kilograms conversion", "Shrinkage %", or "Rework loops". TexSetu’s data models reflect the exact operational vernacular of the textile cluster.
2. **Network Density in Surat**: By concentrating initial operational execution within a 20-kilometer radius in Surat, TexSetu achieves high local liquidity where buyers and providers already have commercial proximity.
