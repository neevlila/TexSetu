package com.texsetu.entity;

import com.texsetu.entity.enums.ComplaintStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "complaints")
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "complaint_reference", nullable = false, unique = true, length = 50)
    private String complaintReference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private JobOrder order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complainant_id", nullable = false)
    private User complainant;

    @NotBlank
    @Column(nullable = false, length = 50)
    private String category; // QUALITY_DEFECT, DELAY, SHORTAGE, BILLING, OTHER

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "evidence_url", length = 500)
    private String evidenceUrl;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ComplaintStatus status = ComplaintStatus.OPEN;

    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Complaint() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getComplaintReference() { return complaintReference; }
    public void setComplaintReference(String complaintReference) { this.complaintReference = complaintReference; }

    public JobOrder getOrder() { return order; }
    public void setOrder(JobOrder order) { this.order = order; }

    public User getComplainant() { return complainant; }
    public void setComplainant(User complainant) { this.complainant = complainant; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getEvidenceUrl() { return evidenceUrl; }
    public void setEvidenceUrl(String evidenceUrl) { this.evidenceUrl = evidenceUrl; }

    public ComplaintStatus getStatus() { return status; }
    public void setStatus(ComplaintStatus status) { this.status = status; }

    public String getAdminNotes() { return adminNotes; }
    public void setAdminNotes(String adminNotes) { this.adminNotes = adminNotes; }

    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
