package com.texsetu.entity;

import com.texsetu.entity.enums.OrderStatus;
import com.texsetu.entity.enums.UnitOfMeasure;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_orders")
public class JobOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "order_number", nullable = false, unique = true, length = 40)
    private String orderNumber;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quotation_id", nullable = false, unique = true)
    private Quotation quotation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rfq_id", nullable = false)
    private Rfq rfq;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_worker_id", nullable = false)
    private JobWorkerProfile jobWorker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private ServiceCategory category;

    @NotNull
    @Column(name = "agreed_quantity", nullable = false, precision = 12, scale = 2)
    private BigDecimal agreedQuantity;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "unit_of_measure", nullable = false, length = 20)
    private UnitOfMeasure unitOfMeasure;

    @NotNull
    @Column(name = "agreed_price", nullable = false, precision = 14, scale = 2)
    private BigDecimal agreedPrice;

    @NotNull
    @Column(name = "expected_completion_date", nullable = false)
    private LocalDate expectedCompletionDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "current_status", nullable = false, length = 30)
    private OrderStatus currentStatus = OrderStatus.CONFIRMED;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ProductionLot productionLot;

    @Version
    @Column(nullable = false)
    private Long version = 0L;

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

    public JobOrder() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public Quotation getQuotation() { return quotation; }
    public void setQuotation(Quotation quotation) { this.quotation = quotation; }

    public Rfq getRfq() { return rfq; }
    public void setRfq(Rfq rfq) { this.rfq = rfq; }

    public User getBuyer() { return buyer; }
    public void setBuyer(User buyer) { this.buyer = buyer; }

    public JobWorkerProfile getJobWorker() { return jobWorker; }
    public void setJobWorker(JobWorkerProfile jobWorker) { this.jobWorker = jobWorker; }

    public ServiceCategory getCategory() { return category; }
    public void setCategory(ServiceCategory category) { this.category = category; }

    public BigDecimal getAgreedQuantity() { return agreedQuantity; }
    public void setAgreedQuantity(BigDecimal agreedQuantity) { this.agreedQuantity = agreedQuantity; }

    public UnitOfMeasure getUnitOfMeasure() { return unitOfMeasure; }
    public void setUnitOfMeasure(UnitOfMeasure unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; }

    public BigDecimal getAgreedPrice() { return agreedPrice; }
    public void setAgreedPrice(BigDecimal agreedPrice) { this.agreedPrice = agreedPrice; }

    public LocalDate getExpectedCompletionDate() { return expectedCompletionDate; }
    public void setExpectedCompletionDate(LocalDate expectedCompletionDate) { this.expectedCompletionDate = expectedCompletionDate; }

    public OrderStatus getCurrentStatus() { return currentStatus; }
    public void setCurrentStatus(OrderStatus currentStatus) { this.currentStatus = currentStatus; }

    public ProductionLot getProductionLot() { return productionLot; }
    public void setProductionLot(ProductionLot productionLot) { this.productionLot = productionLot; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
