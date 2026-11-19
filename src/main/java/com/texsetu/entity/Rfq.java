package com.texsetu.entity;

import com.texsetu.entity.enums.RfqStatus;
import com.texsetu.entity.enums.UnitOfMeasure;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rfqs")
public class Rfq {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "rfq_number", nullable = false, unique = true, length = 40)
    private String rfqNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private ServiceCategory category;

    @NotBlank
    @Column(nullable = false, length = 200)
    private String title;

    @NotBlank
    @Column(name = "fabric_description", nullable = false, columnDefinition = "TEXT")
    private String fabricDescription;

    @NotNull
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal quantity;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "unit_of_measure", nullable = false, length = 20)
    private UnitOfMeasure unitOfMeasure;

    @NotNull
    @Column(name = "desired_completion_date", nullable = false)
    private LocalDate desiredCompletionDate;

    @NotBlank
    @Column(name = "processing_requirements", nullable = false, columnDefinition = "TEXT")
    private String processingRequirements;

    @Column(name = "technical_specifications", columnDefinition = "TEXT")
    private String technicalSpecifications;

    @Column(name = "delivery_requirements", columnDefinition = "TEXT")
    private String deliveryRequirements;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RfqStatus status = RfqStatus.OPEN;

    @Version
    @Column(nullable = false)
    private Long version = 0L;

    @OneToMany(mappedBy = "rfq", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RfqAttachment> attachments = new ArrayList<>();

    @OneToMany(mappedBy = "rfq", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Quotation> quotations = new ArrayList<>();

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

    public Rfq() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRfqNumber() { return rfqNumber; }
    public void setRfqNumber(String rfqNumber) { this.rfqNumber = rfqNumber; }

    public User getBuyer() { return buyer; }
    public void setBuyer(User buyer) { this.buyer = buyer; }

    public ServiceCategory getCategory() { return category; }
    public void setCategory(ServiceCategory category) { this.category = category; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getFabricDescription() { return fabricDescription; }
    public void setFabricDescription(String fabricDescription) { this.fabricDescription = fabricDescription; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public UnitOfMeasure getUnitOfMeasure() { return unitOfMeasure; }
    public void setUnitOfMeasure(UnitOfMeasure unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; }

    public LocalDate getDesiredCompletionDate() { return desiredCompletionDate; }
    public void setDesiredCompletionDate(LocalDate desiredCompletionDate) { this.desiredCompletionDate = desiredCompletionDate; }

    public String getProcessingRequirements() { return processingRequirements; }
    public void setProcessingRequirements(String processingRequirements) { this.processingRequirements = processingRequirements; }

    public String getTechnicalSpecifications() { return technicalSpecifications; }
    public void setTechnicalSpecifications(String technicalSpecifications) { this.technicalSpecifications = technicalSpecifications; }

    public String getDeliveryRequirements() { return deliveryRequirements; }
    public void setDeliveryRequirements(String deliveryRequirements) { this.deliveryRequirements = deliveryRequirements; }

    public RfqStatus getStatus() { return status; }
    public void setStatus(RfqStatus status) { this.status = status; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public List<RfqAttachment> getAttachments() { return attachments; }
    public void setAttachments(List<RfqAttachment> attachments) { this.attachments = attachments; }

    public List<Quotation> getQuotations() { return quotations; }
    public void setQuotations(List<Quotation> quotations) { this.quotations = quotations; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
