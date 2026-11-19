# 5-Minute Live Demonstration Script — TexSetu

## 1. Demo Objectives & Presenter Guidelines

This script is structured for a rapid, high-impact 5-minute walkthrough to present to an academic examination committee or venture capital panel. It showcases the core transaction loop across all three personas: **Buyer**, **Job-Worker**, and **Administrator**.

### Quick-Access Demo Credentials (Pre-Seeded)
The local development server (`mvnw.cmd spring-boot:run`) runs with an in-memory database pre-populated with realistic Surat market entities. Quick-fill buttons are also available directly on the login page (`/login`):

| Persona | Email Address | Password | Role & Context |
|:---|:---|:---|:---|
| **Textile Buyer** | `buyer@texsetu.com` | `Buyer@123` | Rajeshbhai Shah (Radha Silk Mills, STM Market) |
| **Job-Worker (Mill)**| `worker@texsetu.com` | `Worker@123` | Vikram Patel (Shiv Dyeing & Printing, Pandesara) |
| **Marketplace Admin**| `admin@texsetu.com` | `Admin@123` | Platform Administrator & Verifier |

---

## 2. Timed 5-Minute Demonstration Walkthrough

```
[00:00 - 00:45] Intro & Supplier Discovery
      │
[00:45 - 01:45] Buyer Creates RFQ & Compares Quotations
      │
[01:45 - 02:45] Atomic Acceptance & Work Order Crystallization
      │
[02:45 - 03:45] Job-Worker Inward Reconciliation & Lot Passport
      │
[03:45 - 04:30] Quality Inspection & Rework Loop Demonstration
      │
[04:30 - 05:00] Admin Governance, Dispute Queue & CSV Export
```

---

### Segment 1: Public Marketplace & Supplier Discovery (00:00 – 00:45)
1. **Open Browser** to `http://localhost:8080/`.
2. **Highlight Identity**: Point out the Industrial Navy & Teal design system, Surat textile cluster focus, and the 3 core processing verticals: *Dyeing & Processing*, *Printing & Embroidery*, and *Garment Stitching*.
3. **Navigate to Discovery** (`/discovery`):
   - Show faceted search filters: Category, Industrial Area (Pandesara, Sachin, Udhna), and Minimum Order Quantity (MOQ).
   - Point out the **Verified Status Badge** and **Indicative Available Capacity** with freshness timestamp ("Updated today").
   - Click **View Profile** on `Shiv Dyeing & Printing Mills` to show the full machinery capability catalog.

---

### Segment 2: Buyer RFQ & Side-by-Side Quotation Comparison (00:45 – 01:45)
1. **Navigate to Login** (`/login`) and click the quick-fill button **"Buyer Demo"** (`buyer@texsetu.com` / `Buyer@123`).
2. **Buyer Dashboard**: Point out the active RFQ statistics and orders summary.
3. **Open RFQ Management** (`/buyer/rfqs`):
   - Click on the pre-seeded RFQ: `RFQ-20261119-0001` ("Viscose Georgette Soft Flow Dyeing - 5,000 Meters").
4. **The Quotation Comparison Matrix** (`/buyer/rfqs/compare/1`):
   - Show how competing quotes from Pandesara and Sachin mills are rendered side-by-side.
   - Point out the **"Lowest Price"** badge (₹22.50/m) and **"Fastest Turnaround"** badge (5 days).

---

### Segment 3: Atomic Quotation Acceptance & Work Order Creation (01:45 – 02:45)
1. In the comparison matrix, click **"Accept Quotation"** on Shiv Dyeing's quote.
2. **Explain the Concurrency Guard**:
   - Point out that under the hood, JPA `@Version` and transactional logic atomically mark the winning quote `ACCEPTED`, change the RFQ to `CLOSED`, auto-reject competing bids, and spawn `JobOrder` (`ORD-20261119-XXXX`).
3. **Redirect to Order Details**:
   - Show that the order has been created in status `PLACED`.
   - Point out the **Digital Production Lot Passport** section with its generated ID (`LOT-YYYYMMDD-XXXX`).

---

### Segment 4: Job-Worker Inwarding & Lot Passport Progression (02:45 – 03:45)
1. **Switch Persona**: Logout and log in as **Job-Worker** (`worker@texsetu.com` / `Worker@123`).
2. **Job-Worker Dashboard** (`/worker/dashboard`):
   - View pending orders and incoming work.
3. **Open the Order Details** (`/worker/orders/view/1`):
   - Click the button **"Record Inward Material"**.
   - Input: Challan Quantity: `5,000 Meters`, Actual Received: `4,985 Meters`, Notes: `Rolls received in good condition, minor selvage moisture on roll 3`.
   - Submit: Notice the order status updates to `MATERIAL_INWARDED` and the inward reconciliation event is permanently logged to the Lot Passport timeline.
4. **Advance Production Stage**:
   - Click **"Update Production Stage"** -> Select `Scouring & Bleaching` -> Add note: `Pre-treatment completed` -> Click Submit.
   - Point out the visual timeline stepper dynamically rendering the timestamped milestone.

---

### Segment 5: Quality Inspection, Rework Loop & Settlement (03:45 – 04:30)
1. **Switch Persona**: Log back in as **Buyer** (`buyer@texsetu.com`).
2. **Open Order View**: Show the order now in `INSPECTION` stage.
3. **Demonstrate the Rework Safeguard**:
   - Click **"Perform Quality Inspection"**.
   - Select **"REWORK REQUIRED"**, give Score: `60`, Defect Notes: `Uneven shade density on batch rolls 2 and 4`.
   - Submit: Show the audience that the system **automatically reverts the order status back to PROCESSING** and appends the defect audit event into the Lot Passport timeline!
4. **Final Approval**:
   - Re-open inspection, select **"ACCEPTED"**, Score: `95`, Notes: `Re-dyeing completed perfectly. Shade matches master swatch.`
   - Submit: Show the order cleanly transitioning to `COMPLETED`, triggering simulated escrow payment release and unlocking the permanent review form.

---

### Segment 6: Admin Governance & Operational CSV Export (04:30 – 05:00)
1. **Switch Persona**: Log in as **Admin** (`admin@texsetu.com` / `Admin@123`).
2. **Admin Dashboard** (`/admin/dashboard`):
   - View platform GMV, user breakdown, and verification requests.
3. **Verification Queue** (`/admin/verifications`):
   - Show pending job-workers with factory addresses and machinery specifications awaiting verification.
4. **Streaming CSV Operational Exports**:
   - Click **"Export Orders CSV"** -> Open the downloaded CSV file to show full relational columns, order numbers, amounts, and lot passport identifiers ready for enterprise auditing.

**Conclusion**: Conclude by reiterating that TexSetu is not a concept or mockup, but a fully functional, concurrency-safe, test-verified software platform built for India's textile heartland.
