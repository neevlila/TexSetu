package com.texsetu.dto;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public class QuotationSubmitDto {

    @NotNull(message = "RFQ ID is required")
    private Long rfqId;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.01", message = "Unit price must be positive")
    private BigDecimal unitPrice;

    @NotNull(message = "Total price is required")
    @DecimalMin(value = "0.01", message = "Total price must be positive")
    private BigDecimal totalPrice;

    private BigDecimal minQuantityConditions;

    @NotNull(message = "Estimated duration is required")
    @Min(value = 1, message = "Duration must be at least 1 day")
    private Integer estimatedDurationDays;

    @NotNull(message = "Proposed completion date is required")
    @Future(message = "Proposed completion date must be in the future")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate proposedCompletionDate;

    private String termsConditions;

    @NotNull(message = "Validity date is required")
    @Future(message = "Validity date must be in the future")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate validityDate;

    private String additionalNotes;

    public QuotationSubmitDto() {}

    // Getters and Setters
    public Long getRfqId() { return rfqId; }
    public void setRfqId(Long rfqId) { this.rfqId = rfqId; }

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
}
