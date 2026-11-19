package com.texsetu.dto;

import com.texsetu.entity.enums.InspectionResult;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

public class InspectionCreateDto {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotNull(message = "Quantity inspected is required")
    @DecimalMin(value = "0.1", message = "Quantity must be positive")
    private BigDecimal quantityInspected;

    @NotNull(message = "Accepted quantity is required")
    private BigDecimal acceptedQuantity;

    private BigDecimal rejectedQuantity = BigDecimal.ZERO;

    private String defectDetails;

    @NotBlank(message = "Inspection comments are required")
    private String inspectionComments;

    @NotNull(message = "Inspection result is required")
    private InspectionResult result;

    private MultipartFile evidencePhoto;

    public InspectionCreateDto() {}

    // Getters and Setters
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

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

    public MultipartFile getEvidencePhoto() { return evidencePhoto; }
    public void setEvidencePhoto(MultipartFile evidencePhoto) { this.evidencePhoto = evidencePhoto; }
}
