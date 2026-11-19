package com.texsetu.entity;

import com.texsetu.entity.enums.UnitOfMeasure;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "service_capabilities")
public class ServiceCapability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_worker_id", nullable = false)
    private JobWorkerProfile jobWorker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private ServiceCategory category;

    @NotBlank
    @Column(name = "service_name", nullable = false, length = 150)
    private String serviceName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Column(name = "minimum_quantity", nullable = false, precision = 12, scale = 2)
    private BigDecimal minimumQuantity;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "unit_of_measure", nullable = false, length = 20)
    private UnitOfMeasure unitOfMeasure;

    @NotNull
    @Column(name = "indicative_price_per_unit", nullable = false, precision = 12, scale = 2)
    private BigDecimal indicativePricePerUnit;

    @Column(name = "lead_time_days", nullable = false)
    private Integer leadTimeDays = 3;

    @Column(name = "processing_details", columnDefinition = "TEXT")
    private String processingDetails;

    @Column(nullable = false)
    private Boolean active = true;

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

    public ServiceCapability() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public JobWorkerProfile getJobWorker() { return jobWorker; }
    public void setJobWorker(JobWorkerProfile jobWorker) { this.jobWorker = jobWorker; }

    public ServiceCategory getCategory() { return category; }
    public void setCategory(ServiceCategory category) { this.category = category; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getMinimumQuantity() { return minimumQuantity; }
    public void setMinimumQuantity(BigDecimal minimumQuantity) { this.minimumQuantity = minimumQuantity; }

    public UnitOfMeasure getUnitOfMeasure() { return unitOfMeasure; }
    public void setUnitOfMeasure(UnitOfMeasure unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; }

    public BigDecimal getIndicativePricePerUnit() { return indicativePricePerUnit; }
    public void setIndicativePricePerUnit(BigDecimal indicativePricePerUnit) { this.indicativePricePerUnit = indicativePricePerUnit; }

    public Integer getLeadTimeDays() { return leadTimeDays; }
    public void setLeadTimeDays(Integer leadTimeDays) { this.leadTimeDays = leadTimeDays; }

    public String getProcessingDetails() { return processingDetails; }
    public void setProcessingDetails(String processingDetails) { this.processingDetails = processingDetails; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
