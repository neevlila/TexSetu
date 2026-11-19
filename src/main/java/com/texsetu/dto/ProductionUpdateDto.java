package com.texsetu.dto;

import com.texsetu.entity.enums.OrderStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

public class ProductionUpdateDto {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotNull(message = "Next order status is required")
    private OrderStatus nextStatus;

    @NotBlank(message = "Stage description is required")
    private String stageName;

    @NotBlank(message = "Event description is required")
    private String description;

    private BigDecimal completedQuantity;
    private BigDecimal rejectedWasteQuantity;
    private MultipartFile attachment;

    public ProductionUpdateDto() {}

    // Getters and Setters
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public OrderStatus getNextStatus() { return nextStatus; }
    public void setNextStatus(OrderStatus nextStatus) { this.nextStatus = nextStatus; }

    public String getStageName() { return stageName; }
    public void setStageName(String stageName) { this.stageName = stageName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getCompletedQuantity() { return completedQuantity; }
    public void setCompletedQuantity(BigDecimal completedQuantity) { this.completedQuantity = completedQuantity; }

    public BigDecimal getRejectedWasteQuantity() { return rejectedWasteQuantity; }
    public void setRejectedWasteQuantity(BigDecimal rejectedWasteQuantity) { this.rejectedWasteQuantity = rejectedWasteQuantity; }

    public MultipartFile getAttachment() { return attachment; }
    public void setAttachment(MultipartFile attachment) { this.attachment = attachment; }
}
