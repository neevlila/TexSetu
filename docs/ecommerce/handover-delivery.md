# Physical Handover & Delivery Management — TexSetu

## 1. Physical Logistics Realities in the Surat Textile Hub

Unlike retail e-commerce where lightweight packages are transported by national courier networks (e.g., BlueDart, Delhivery), textile job-work involves heavy, high-volume bulk commodities:
- A single batch of 5,000 meters of viscose fabric weighs between **700 kg and 1,200 kg**.
- Intra-cluster transport between wholesale markets (Ring Road) and industrial processing zones (Pandesara, Sachin, Palsana) is handled by specialized local commercial vehicles:
  - Three-wheeled auto tempos (for sampling and small rolls < 300 kg).
  - Open-bed Tata 407 / Eicher trucks (for full 5,000 to 20,000-meter production lots).
- Transport is predominantly contracted directly by the buyer or managed by factory dispatch desks via local tempo unions.

---

## 2. Inward & Outward Custody Protocol

To protect against physical loss, theft, and damage during transit, TexSetu introduces standardized custody checkpoints:

```
[Buyer Dispatches Grey Fabric]
        │  • Generates TexSetu Outward Challan Reference
        │  • Notes dispatched rolls and declared meters
        ▼
[Transit via Local Tempo / Transporter]
        ▼
[Job-Worker Inward Gate Checkpoint]
        │  • Digital Inwarding on TexSetu Portal:
        │    - Physical roll count verified.
        │    - Actual gross meters measured and recorded.
        │    - Visual damage / soil stains photographed and attached.
        ▼
[Material Inward Reconciliation Event Logged]
        │  • Variance flag triggered if shrinkage / shortage > 1.5%
        ▼
[Processing Mill Production Floor]
        ▼
[Finished Goods Outward Dispatch]
        │  • Final packed roll count logged.
        │  • Finished meters after chemical shrinkage recorded.
        │  • Transporter vehicle number and driver phone recorded.
        ▼
[Buyer Receiving Warehouse]
        │  • Formal Quality Inspection performed on-site.
        │  • Acceptance sign-off unlocks payment release.
```

---

## 3. Discrepancy & Shrinkage Tolerances

### Natural Technical Processing Shrinkage
During scouring, chemical dyeing, and stentering, fabrics naturally contract:
- **Synthetic Fabrics (Polyester/Nylon)**: Typical shrinkage tolerance of **2% to 4%**.
- **Regenerated Cellulose (Viscose/Rayon)**: Typical shrinkage tolerance of **4% to 8%**.
- **Cotton / Spun Yarns**: Typical shrinkage tolerance of **5% to 7%**.

### TexSetu Reconciliation Rules
1. **Normal Processing Loss**: Shortfalls within the pre-agreed quotation tolerance percentage are automatically categorized as standard processing loss and do not trigger order disputes.
2. **Excessive Shortfall Alert**: If actual finished meters delivered fall short of `(Inwarded Quantity * (1 - Allowed Shrinkage%))`, the platform generates a **Quantity Shortage Warning**, requiring the job-worker to submit a technical justification (e.g., cutting off oil-stained selvages) before the buyer completes inspection.
