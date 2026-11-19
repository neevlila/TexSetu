package com.texsetu.entity;

import com.texsetu.entity.enums.QuotationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "quotations")
public class Quotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "quote_number", nullable = false, unique = true, length = 40)
    private String quoteNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rfq_id", nullable = false)
    private Rfq rfq;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_worker_id", nullable = false)
    private JobWorkerProfile jobWorker;

    @NotNull
    @DecimalMin(value = "0.01", message = "Unit price must be positive")
    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @NotNull
    @DecimalMin(value = "0.01", message = "Total price must be positive")
    @Column(name = "total_price", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "min_quantity_conditions", precision = 12, scale = 2)
    private BigDecimal minQuantityConditions;

    @NotNull
    @Column(name = "estimated_duration_days", nullable = false)
    private Integer estimatedDurationDays;

    @NotNull
    @Column(name = "proposed_completion_date", nullable = false)
    private LocalDate proposedCompletionDate;

    @Column(name = "terms_conditions", columnDefinition = "TEXT")
    private String termsConditions;

    @NotNull
    @Column(name = "validity_date", nullable = false)
    private LocalDate validityDate;

    @Column(name = "additional_notes", columnDefinition = "TEXT")
    private String additionalNotes;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private QuotationStatus status = QuotationStatus.SUBMITTED;

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

    public Quotation() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getQuoteNumber() { return quoteNumber; }
    public void setQuoteNumber(String quoteNumber) { this.quoteNumber = quoteNumber; }

    public Rfq getRfq() { return rfq; }
    public void setRfq(Rfq rfq) { this.rfq = rfq; }

    public JobWorkerProfile getJobWorker() { return jobWorker; }
    public void setJobWorker(JobWorkerProfile jobWorker) { this.jobWorker = jobWorker; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public BigDecimal getMinQuantityConditions() { return minQuantityConditions; }
    public void setMinQuantityConditions(BigDecimal minQuantityConditions) { this.minQuantityConditions = minQuantityConditions; }

    public Integer getEstimatedDurationDays() { return estimatedDurationDays; }
    public void setEstimatedDurationDays(Integer estimatedDurationDays) { this.estimatedDurationDays = estimatedDurationDays; }

    public LocalDate getProposedCompletionDate() { return proposedCompletionDate; }
    public void setProposedCompletionDate(LocalDate proposedCompletionDate) { this.proposedCompletionDate = proposedCompletionDate; }

    public String getTermsConditions() { return termsConditions; }
    public void setTermsConditions(String termsConditions) { this.termsConditions = termsConditions; }

    public LocalDate getValidityDate() { return validityDate; }
    public void setValidityDate(LocalDate validityDate) { this.validityDate = validityDate; }

    public String getAdditionalNotes() { return additionalNotes; }
    public void setAdditionalNotes(String additionalNotes) { this.additionalNotes = additionalNotes; }

    public QuotationStatus getStatus() { return status; }
    public void setStatus(QuotationStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
