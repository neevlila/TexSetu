# Payment Approach & Escrow Simulation — TexSetu

## 1. Commercial Payment Realities in Textile Manufacturing

Payment terms in the Indian textile job-work sector represent one of the most contentious friction points:
- **Buyer Exposure**: Fear of releasing upfront advances for orders that may result in damaged goods, color bleeding, or excessive shrinkage.
- **Job-Worker Exposure**: Fear of delivering processed fabric and having payments arbitrarily withheld, delayed past 90 days, or reduced through unwarranted "quality cut" deductions.

To balance these commercial tensions, TexSetu implements a **Digital Escrow Simulation Workflow** designed to model secure, milestone-gated B2B payments without incurring premature PCI-DSS or regulatory licensing overhead during the initial MVP.

---

## 2. Simulated Escrow Ledger Architecture

```
                    ┌───────────────────────────────┐
                    │      ORDER PLACED             │
                    │   (Contract Value: ₹1,20,000) │
                    └───────────────┬───────────────┘
                                    │ Buyer Initiates Escrow
                                    ▼
                    ┌───────────────────────────────┐
                    │       HELD_IN_ESCROW          │
                    │  (Funds secured in virtual    │
                    │   escrow vault)               │
                    └───────────────┬───────────────┘
                                    │
                    ┌───────────────┴───────────────┐
                    │                               │
        Quality Accepted                            Dispute / Rejection
                    ▼                                       ▼
    ┌───────────────────────────────┐       ┌───────────────────────────────┐
    │     RELEASED_TO_WORKER        │       │      REFUNDED_TO_BUYER        │
    │  (Funds disbursed to mill     │       │  (Funds returned to buyer via │
    │   account upon handover)      │       │   admin dispute resolution)   │
    └───────────────────────────────┘       └───────────────────────────────┘
```

---

## 3. Implementation Details: `MockPaymentGateway`

The simulated payment engine is implemented in Java under `com.texsetu.service.MockPaymentGateway`.

### 3.1 State Operations
1. **`initiateEscrowPayment(JobOrder order, String method)`**:
   - Generates a unique transaction reference: `TXN-ESCROW-YYYYMMDD-XXXX`.
   - Records payment amount matching the accepted contract value.
   - Sets state to `HELD_IN_ESCROW`.
   - Fires an asynchronous notification to both parties confirming funds are secured.
2. **`releaseEscrowPayment(JobOrder order)`**:
   - Validates that the order has achieved formal `INSPECTION` outcome: `ACCEPTED`.
   - Transitions payment state to `RELEASED_TO_WORKER`.
   - Creates an auditable transaction settlement timestamp.
3. **`refundEscrowPayment(JobOrder order, String reason)`**:
   - Authorized only for administrators or mutual buyer-worker cancellation before material inwarding.
   - Transitions state to `REFUNDED_TO_BUYER`.

### 3.2 Supported Mock Payment Rails
- **Bank Transfer (NEFT / RTGS)**: Standard for commercial transactions exceeding ₹2,00,000.
- **Corporate UPI / Virtual Accounts**: High-velocity settlement for short-run sampling lots (< ₹1,00,000).
- **Post-Dated Cheque (PDC) Registration**: Allows recording of traditional textile market credit instruments with digital maturity reminders.

---

## 4. Production Roadmap: Transition to Regulated Payment Rails

For production scaling beyond the academic/pilot phase, TexSetu will integrate with RBI-licensed digital escrow and nodal account aggregators:
1. **Escrow Nodal Bank Partnership**: Integration with ICICI Bank / YES Bank Smart Nodal Accounts to legally hold funds under RBI Master Directions for Payment Aggregators.
2. **Automated Split-Payments (Razorpay Route / Cashfree Split)**: Automatic splitting of transaction amounts at settlement: 98.75% directly to the job-worker’s bank account and 1.25% marketplace take-rate retained by TexSetu.
3. **e-NACH Mandates**: Recurring subscription collection for Verified Pro and Enterprise mill SaaS plans.
