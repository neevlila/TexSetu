package com.texsetu.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "production_lots")
public class ProductionLot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "lot_number", nullable = false, unique = true, length = 40)
    private String lotNumber;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private JobOrder order;

    @NotNull
    @Column(name = "original_submitted_quantity", nullable = false, precision = 12, scale = 2)
    private BigDecimal originalSubmittedQuantity;

    @NotNull
    @Column(name = "received_quantity", nullable = false, precision = 12, scale = 2)
    private BigDecimal receivedQuantity;

    @NotBlank
    @Column(name = "processing_category", nullable = false, length = 100)
    private String processingCategory;

    @Column(name = "processing_notes", columnDefinition = "TEXT")
    private String processingNotes;

    @NotBlank
    @Column(name = "current_stage", nullable = false, length = 50)
    private String currentStage = "LOT_INITIALIZED";

    @NotNull
    @Column(name = "quantity_completed", nullable = false, precision = 12, scale = 2)
    private BigDecimal quantityCompleted = BigDecimal.ZERO;

    @NotNull
    @Column(name = "quantity_rejected_waste", nullable = false, precision = 12, scale = 2)
    private BigDecimal quantityRejectedWaste = BigDecimal.ZERO;

    @NotBlank
    @Column(nullable = false, length = 30)
    private String status = "ACTIVE"; // ACTIVE, ON_HOLD, REWORK, CLOSED

    @OneToMany(mappedBy = "productionLot", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("eventTimestamp ASC")
    private List<ProductionEvent> events = new ArrayList<>();

    @OneToMany(mappedBy = "productionLot", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QualityInspection> inspections = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public ProductionLot() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLotNumber() { return lotNumber; }
    public void setLotNumber(String lotNumber) { this.lotNumber = lotNumber; }

    public JobOrder getOrder() { return order; }
    public void setOrder(JobOrder order) { this.order = order; }

    public BigDecimal getOriginalSubmittedQuantity() { return originalSubmittedQuantity; }
    public void setOriginalSubmittedQuantity(BigDecimal originalSubmittedQuantity) { this.originalSubmittedQuantity = originalSubmittedQuantity; }

    public BigDecimal getReceivedQuantity() { return receivedQuantity; }
    public void setReceivedQuantity(BigDecimal receivedQuantity) { this.receivedQuantity = receivedQuantity; }

    public String getProcessingCategory() { return processingCategory; }
    public void setProcessingCategory(String processingCategory) { this.processingCategory = processingCategory; }

    public String getProcessingNotes() { return processingNotes; }
    public void setProcessingNotes(String processingNotes) { this.processingNotes = processingNotes; }

    public String getCurrentStage() { return currentStage; }
    public void setCurrentStage(String currentStage) { this.currentStage = currentStage; }

    public BigDecimal getQuantityCompleted() { return quantityCompleted; }
    public void setQuantityCompleted(BigDecimal quantityCompleted) { this.quantityCompleted = quantityCompleted; }

    public BigDecimal getQuantityRejectedWaste() { return quantityRejectedWaste; }
    public void setQuantityRejectedWaste(BigDecimal quantityRejectedWaste) { this.quantityRejectedWaste = quantityRejectedWaste; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<ProductionEvent> getEvents() { return events; }
    public void setEvents(List<ProductionEvent> events) { this.events = events; }

    public List<QualityInspection> getInspections() { return inspections; }
    public void setInspections(List<QualityInspection> inspections) { this.inspections = inspections; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
