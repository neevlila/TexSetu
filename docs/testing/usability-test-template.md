# Usability & User Acceptance Testing (UAT) Protocol — TexSetu

## 1. UAT Test Protocol & Participant Profiles

User Acceptance Testing is designed to evaluate whether the digital workflow can be smoothly executed by users with varying technical literacy, from second-generation textile traders to factory floor dispatch supervisors.

### Participant Cohorts
- **Cohort A (Buyers)**: 3 Surat textile merchant traders operating from Millennium Market and STM.
- **Cohort B (Job-Workers)**: 3 Mill managers / dispatch supervisors from Pandesara GIDC and Sachin GIDC.
- **Cohort C (System Admins)**: 2 Technical operators evaluating verification and dispute handling.

---

## 2. Structured Task Scenarios & Evaluation Rubric

### Scenario A: Buyer Workflow (RFQ to Quote Acceptance)
- **Task 1**: Search for a verified dyeing provider located in Pandesara with MOQ <= 2,000 meters.
- **Task 2**: Create a Request for Quotation for 5,000 meters of Viscose Georgette fabric.
- **Task 3**: Open the Quotation Comparison Matrix and evaluate competing bids.
- **Task 4**: Accept the winning quotation and verify the creation of the Work Order.
- **Success Criteria**: Completed without moderator intervention in < 5 minutes; SUS (System Usability Scale) score > 80.

### Scenario B: Job-Worker Workflow (Capacity & Lot Inwarding)
- **Task 1**: Update mill daily processing capacity and view incoming RFQ notifications.
- **Task 2**: Submit a price quotation with unit rate and turnaround days.
- **Task 3**: Open the accepted Job Order and navigate to the Production Lot Passport.
- **Task 4**: Perform Inward Material Reconciliation (record 5,000m challan vs 4,985m actual).
- **Task 5**: Advance production stage to "Scouring & Bleaching" with notes.
- **Success Criteria**: Clear understanding of physical inwarding fields; zero confusion regarding challan vs actual count.

### Scenario C: Quality Inspection & Rework Loop
- **Task 1**: Buyer opens completed lot and initiates Quality Inspection.
- **Task 2**: Record a `REWORK_REQUIRED` outcome due to shade variation in roll #3.
- **Task 3**: Verify that the order status automatically reverts to `PROCESSING` with defect observation logged.
- **Success Criteria**: Buyer and worker both understand that rework is formally tracked without canceling the order.

---

## 3. UAT Feedback Capture Template

```
Participant Name: _______________________  Role: [ ] Buyer  [ ] Job-Worker
Date: _________________________________  Device: [ ] Mobile  [ ] Desktop Browser

Task Rating (1 = Very Confusing, 5 = Highly Intuitive):
1. Provider Discovery & Filters:        [ 1 ] [ 2 ] [ 3 ] [ 4 ] [ 5 ]
2. RFQ Submission:                      [ 1 ] [ 2 ] [ 3 ] [ 4 ] [ 5 ]
3. Quote Comparison Matrix:             [ 1 ] [ 2 ] [ 3 ] [ 4 ] [ 5 ]
4. Inward Material Count Entry:         [ 1 ] [ 2 ] [ 3 ] [ 4 ] [ 5 ]
5. Lot Passport Timeline:               [ 1 ] [ 2 ] [ 3 ] [ 4 ] [ 5 ]
6. Inspection & Defect Reporting:       [ 1 ] [ 2 ] [ 3 ] [ 4 ] [ 5 ]

Qualitative Feedback:
What was the most intuitive feature?
________________________________________________________________________________

Where did you feel friction or hesitate?
________________________________________________________________________________
```
