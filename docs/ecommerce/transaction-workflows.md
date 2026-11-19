# End-to-End Transaction Workflows — TexSetu

## 1. The Core B2B Job-Work Transaction Lifecycle

The core transaction workflow in TexSetu is structured into five sequential, state-enforced phases.

```mermaid
sequenceDiagram
    autonumber
    actor Buyer as Textile Buyer / Trader
    participant TexSetu as TexSetu Core Engine
    actor Worker as Job-Worker / Mill
    participant Passport as Digital Lot Passport

    Note over Buyer,Worker: PHASE 1: DISCOVERY & RFQ
    Buyer->>TexSetu: Search providers (Category, City, Capacity)
    Buyer->>TexSetu: Create RFQ (Fabric specs, 5000m, Target Date)
    TexSetu-->>Worker: Broadcast RFQ to eligible category providers

    Note over Buyer,Worker: PHASE 2: QUOTATION & ATOMIC ACCEPTANCE
    Worker->>TexSetu: Submit Quotation (₹24/m, 5 Days, Validity)
    Buyer->>TexSetu: View Side-by-Side Quotation Comparison Matrix
    Buyer->>TexSetu: Accept Quotation (Optimistic Lock Check)
    TexSetu->>TexSetu: Transition RFQ to CLOSED; reject competing quotes
    TexSetu->>TexSetu: Initialize JobOrder (PLACED) & ProductionLot

    Note over Buyer,Worker: PHASE 3: MATERIAL INWARD & LOT PASSPORT
    Buyer->>Worker: Physical dispatch of Grey Fabric with Challan
    Worker->>TexSetu: Inward Material (Challan: 5000m, Actual Recv: 4985m)
    TexSetu->>Passport: Log Event: INWARD_MATERIAL_RECONCILED
    TexSetu->>TexSetu: Transition Order to MATERIAL_INWARDED

    Note over Buyer,Worker: PHASE 4: PRODUCTION STAGES
    Worker->>TexSetu: Advance Stage (Scouring -> Dyeing -> Stentering)
    TexSetu->>Passport: Append Event with Timestamps & Operator Notes
    Worker->>TexSetu: Mark Order as Ready for Inspection

    Note over Buyer,Worker: PHASE 5: INSPECTION & SETTLEMENT
    Buyer->>TexSetu: Submit Quality Audit (Score, ACCEPTED or REWORK)
    alt Quality Accepted
        TexSetu->>TexSetu: Transition Order to COMPLETED
        TexSetu->>Passport: Log Event: QUALITY_ACCEPTED_FINAL
        TexSetu-->>Worker: Release Escrow Payment
        Buyer->>TexSetu: Submit Review & Rating
    else Defect Found (Rework Required)
        TexSetu->>TexSetu: Revert Order to PROCESSING
        TexSetu->>Passport: Log Event: REWORK_REQUESTED (Defect Notes)
        Worker->>TexSetu: Re-process lot and re-submit
    end
```

---

## 2. Granular Stage Specifications & State Rules

### Stage 1: RFQ Formulation & Validation
- **Input Parameters**: Service Category, Fabric Type, Quantity, Unit (`METERS`, `KGS`, `PIECES`, `BATCHES`), Target Delivery Date, Reference Photos.
- **Validation Constraints**: Quantity must be positive (> 0); delivery date must be in the future (> today); unit must be compatible with selected category.
- **Initial State**: `DRAFT` or `OPEN`.

### Stage 2: Bidding & Quotation Comparison
- Verified providers view open RFQs matching their listed capabilities.
- Providers enter unit rate, turnaround lead time, and validity date.
- Comparison matrix highlights:
  - **Lowest Cost per Unit**
  - **Fastest Turnaround Time**
  - **Quotation Expiration Window**

### Stage 3: Atomic Acceptance & Order Crystallization
- Enforces `@Version` check on `Rfq` to guarantee exactly one quote is accepted.
- Competing quotes automatically transition to `REJECTED`.
- Creates `JobOrder` (`STATUS: PLACED`) and seeds the `ProductionLot` passport with unique identifier `LOT-YYYYMMDD-XXXX`.

### Stage 4: Inwarding & Physical Material Reconciliation
- Upon arrival of raw fabric rolls at the factory gate, the job-worker logs:
  - Physical roll count.
  - Actual meters/kgs received vs. paper challan specification.
  - Notes on pre-existing stains, weaving tears, or moisture.
- Resolves the baseline quantity before chemical processing alters fabric weight.

### Stage 5: Production Event Logging
- Mill supervisors record completion of sequential technical stages:
  - *Dyeing*: Scouring, Bleaching, Dye Bath, Washing, Stenter Finishing.
  - *Printing*: Screen preparation, Print run, Curing, Steaming, Washing.
  - *Stitching*: Cutting, Assembly, Buttoning, Thread Trimming, Pressing.
- Every event stores actor ID, timestamp, and optional image links.

### Stage 6: Quality Inspection & Rework Branching
- **Option A (Accepted)**: Buyer confirms finished lot meets specifications. Order transitions to `COMPLETED`; payment is released; buyer is invited to leave a permanent review.
- **Option B (Rework Required)**: Buyer logs defect details (e.g., "Uneven shade in rolls 4 and 5"). Order status reverts to `PROCESSING`; job-worker is notified to re-stenter or re-dye.
- **Option C (Dispute Lodged)**: Unresolvable defect or major shrinkage discrepancy routes order to `DISPUTED` for administrative mediation.
