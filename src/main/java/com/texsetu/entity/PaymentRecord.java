package com.texsetu.entity;

import com.texsetu.entity.enums.PaymentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_records")
public class PaymentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "transaction_reference", nullable = false, unique = true, length = 50)
    private String transactionReference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private JobOrder order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payer_id", nullable = false)
    private User payer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payee_id", nullable = false)
    private User payee;

    @NotNull
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @NotBlank
    @Column(nullable = false, length = 10)
    private String currency = "INR";

    @NotBlank
    @Column(name = "payment_method", nullable = false, length = 40)
    private String paymentMethod = "SIMULATED_ESCROW";

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "gateway_response", columnDefinition = "TEXT")
    private String gatewayResponse;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public PaymentRecord() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTransactionReference() { return transactionReference; }
    public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }

    public JobOrder getOrder() { return order; }
    public void setOrder(JobOrder order) { this.order = order; }

    public User getPayer() { return payer; }
    public void setPayer(User payer) { this.payer = payer; }

    public User getPayee() { return payee; }
    public void setPayee(User payee) { this.payee = payee; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }

    public String getGatewayResponse() { return gatewayResponse; }
    public void setGatewayResponse(String gatewayResponse) { this.gatewayResponse = gatewayResponse; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
