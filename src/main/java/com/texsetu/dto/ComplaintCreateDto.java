package com.texsetu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public class ComplaintCreateDto {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "Description is required")
    private String description;

    private MultipartFile evidenceFile;

    public ComplaintCreateDto() {}

    // Getters and Setters
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public MultipartFile getEvidenceFile() { return evidenceFile; }
    public void setEvidenceFile(MultipartFile evidenceFile) { this.evidenceFile = evidenceFile; }
}
