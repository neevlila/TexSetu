package com.texsetu.dto;

import com.texsetu.entity.enums.UnitOfMeasure;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;

public class RfqCreateDto {

    @NotNull(message = "Service category is required")
    private Long categoryId;

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @NotBlank(message = "Fabric description is required")
    private String fabricDescription;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "1.0", message = "Quantity must be at least 1")
    private BigDecimal quantity;

    @NotNull(message = "Unit of measure is required")
    private UnitOfMeasure unitOfMeasure;

    @NotNull(message = "Desired completion date is required")
    @Future(message = "Completion date must be in the future")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate desiredCompletionDate;

    @NotBlank(message = "Processing requirements are required")
    private String processingRequirements;

    private String technicalSpecifications;
    private String deliveryRequirements;
    private MultipartFile attachment;

    public RfqCreateDto() {}

    // Getters and Setters
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

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

    public MultipartFile getAttachment() { return attachment; }
    public void setAttachment(MultipartFile attachment) { this.attachment = attachment; }
}
