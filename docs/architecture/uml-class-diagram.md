# UML Class Diagram & Domain Model — TexSetu

## 1. Domain Object Model Overview

TexSetu follows Domain-Driven Design (DDD) principles where entities encapsulate identity, invariants, and lifecycle transitions.

```mermaid
classDiagram
    direction TB

    class User {
        +Long id
        +String email
        +String password
        +Role role
        +AccountStatus status
        +String fullName
        +String phoneNumber
        +LocalDateTime createdAt
    }

    class BusinessProfile {
        +Long id
        +String companyName
        +String businessType
        +String gstin
        +String panNumber
        +String addressLine1
        +String city
        +String state
        +String pincode
    }

    class JobWorkerProfile {
        +Long id
        +String factoryAddress
        +String industrialArea
        +String machineryDetails
        +BigDecimal dailyCapacity
        +String capacityUnit
        +LocalDateTime capacityUpdatedAt
        +VerificationStatus verificationStatus
    }

    class ServiceCategory {
        +Long id
        +String name
        +String code
        +String description
        +Boolean isActive
    }

    class ServiceCapability {
        +Long id
        +String serviceName
        +BigDecimal minOrderQuantity
        +String unit
        +BigDecimal indicativePricePerUnit
        +Integer turnaroundDays
        +Boolean isAvailable
    }

    class Rfq {
        +Long id
        +String rfqNumber
        +String title
        +String fabricType
        +BigDecimal quantity
        +UnitOfMeasure unit
        +LocalDate targetCompletionDate
        +RfqStatus status
        +Integer version
    }

    class Quotation {
        +Long id
        +BigDecimal unitPrice
        +BigDecimal totalAmount
        +Integer estimatedTurnaroundDays
        +LocalDate validityDate
        +QuoteStatus status
    }

    class JobOrder {
        +Long id
        +String orderNumber
        +OrderStatus status
        +BigDecimal totalAmount
        +LocalDate targetDeliveryDate
        +Integer version
        +boolean canTransitionTo(OrderStatus newStatus)
    }

    class ProductionLot {
        +Long id
        +String lotNumber
        +BigDecimal challanQuantity
        +BigDecimal actualQuantityReceived
        +String unit
        +String currentStage
        +Boolean isReconciled
        +LocalDateTime materialInwardDate
    }

    class ProductionEvent {
        +Long id
        +String stageName
        +String eventDescription
        +String documentReference
        +LocalDateTime createdAt
    }

    class QualityInspection {
        +Long id
        +InspectionOutcome outcome
        +Integer qualityScore
        +String defectNotes
        +String inspectedBy
        +LocalDateTime inspectedAt
    }

    class PaymentRecord {
        +Long id
        +String transactionRef
        +BigDecimal amount
        +PaymentStatus status
        +String paymentMethod
        +LocalDateTime createdAt
    }

    User "1" --> "0..1" BusinessProfile : owns
    User "1" --> "0..1" JobWorkerProfile : has
    JobWorkerProfile "1" --> "0..*" ServiceCapability : lists
    ServiceCategory "1" <-- "0..*" ServiceCapability : categorizes

    User "1" --> "0..*" Rfq : requests as Buyer
    Rfq "1" --> "0..*" Quotation : receives
    User "1" --> "0..*" Quotation : submits as Worker

    Rfq "1" --> "0..1" JobOrder : originates
    Quotation "1" --> "0..1" JobOrder : accepted for
    JobOrder "1" --> "1" ProductionLot : passport
    ProductionLot "1" --> "0..*" ProductionEvent : audit log

    JobOrder "1" --> "0..*" QualityInspection : inspected in
    JobOrder "1" --> "0..*" PaymentRecord : paid via
```

---

## 2. Enumerated State Models

### 2.1 OrderStatus State Machine Invariants
The `JobOrder` entity implements deterministic lifecycle guarding:
```java
public boolean canTransitionTo(OrderStatus next) {
    return switch (this.status) {
        case PLACED -> next == OrderStatus.MATERIAL_INWARDED || next == OrderStatus.CANCELLED;
        case MATERIAL_INWARDED -> next == OrderStatus.PROCESSING || next == OrderStatus.CANCELLED;
        case PROCESSING -> next == OrderStatus.INSPECTION || next == OrderStatus.DISPUTED;
        case INSPECTION -> next == OrderStatus.COMPLETED || next == OrderStatus.PROCESSING || next == OrderStatus.DISPUTED;
        case COMPLETED, CANCELLED -> false;
        case DISPUTED -> next == OrderStatus.PROCESSING || next == OrderStatus.COMPLETED || next == OrderStatus.CANCELLED;
    };
}
```

### 2.2 Core Enumerations
- **Role**: `BUYER`, `JOB_WORKER`, `ADMIN`
- **AccountStatus**: `PENDING`, `VERIFIED`, `REJECTED`, `SUSPENDED`
- **RfqStatus**: `DRAFT`, `OPEN`, `QUOTED`, `CLOSED`, `CANCELLED`
- **QuoteStatus**: `SUBMITTED`, `ACCEPTED`, `REJECTED`, `EXPIRED`
- **InspectionOutcome**: `ACCEPTED`, `REWORK_REQUIRED`, `REJECTED`
- **PaymentStatus**: `PENDING_ESCROW`, `HELD_IN_ESCROW`, `RELEASED_TO_WORKER`, `REFUNDED_TO_BUYER`
