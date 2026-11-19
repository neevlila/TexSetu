package com.texsetu.entity;

import com.texsetu.entity.enums.InspectionResult;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "quality_inspections")
public class QualityInspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id", nullable = false)
    private ProductionLot productionLot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private JobOrder order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspector_id", nullable = false)
    private User inspector;

    @NotBlank
    @Column(name = "inspector_role", nullable = false, length = 30)
    private String inspectorRole;

    @Column(name = "inspection_date", nullable = false)
    private LocalDateTime inspectionDate = LocalDateTime.now();

    @NotNull
    @Column(name = "quantity_inspected", nullable = false, precision = 12, scale = 2)
    private BigDecimal quantityInspected;

    @NotNull
    @Column(name = "accepted_quantity", nullable = false, precision = 12, scale = 2)
    private BigDecimal acceptedQuantity;

    @NotNull
    @Column(name = "rejected_quantity", nullable = false, precision = 12, scale = 2)
    private BigDecimal rejectedQuantity = BigDecimal.ZERO;

    @Column(name = "defect_details", columnDefinition = "TEXT")
    private String defectDetails;

    @NotBlank
    @Column(name = "inspection_comments", nullable = false, columnDefinition = "TEXT")
    private String inspectionComments;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InspectionResult result;

    @Column(name = "evidence_photo_url", length = 500)
    private String evidencePhotoUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.inspectionDate == null) {
            this.inspectionDate = LocalDateTime.now();
        }
    }

    public QualityInspection() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ProductionLot getProductionLot() { return productionLot; }
    public void setProductionLot(ProductionLot productionLot) { this.productionLot = productionLot; }

    public JobOrder getOrder() { return order; }
    public void setOrder(JobOrder order) { this.order = order; }

    public User getInspector() { return inspector; }
    public void setInspector(User inspector) { this.inspector = inspector; }

    public String getInspectorRole() { return inspectorRole; }
    public void setInspectorRole(String inspectorRole) { this.inspectorRole = inspectorRole; }

    public LocalDateTime getInspectionDate() { return inspectionDate; }
    public void setInspectionDate(LocalDateTime inspectionDate) { this.inspectionDate = inspectionDate; }

    public BigDecimal getQuantityInspected() { return quantityInspected; }
    public void setQuantityInspected(BigDecimal quantityInspected) { this.quantityInspected = quantityInspected; }

    public BigDecimal getAcceptedQuantity() { return acceptedQuantity; }
    public void setAcceptedQuantity(BigDecimal acceptedQuantity) { this.acceptedQuantity = acceptedQuantity; }

    public BigDecimal getRejectedQuantity() { return rejectedQuantity; }
    public void setRejectedQuantity(BigDecimal rejectedQuantity) { this.rejectedQuantity = rejectedQuantity; }

    public String getDefectDetails() { return defectDetails; }
    public void setDefectDetails(String defectDetails) { this.defectDetails = defectDetails; }

    public String getInspectionComments() { return inspectionComments; }
    public void setInspectionComments(String inspectionComments) { this.inspectionComments = inspectionComments; }

    public InspectionResult getResult() { return result; }
    public void setResult(InspectionResult result) { this.result = result; }

    public String getEvidencePhotoUrl() { return evidencePhotoUrl; }
    public void setEvidencePhotoUrl(String evidencePhotoUrl) { this.evidencePhotoUrl = evidencePhotoUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
