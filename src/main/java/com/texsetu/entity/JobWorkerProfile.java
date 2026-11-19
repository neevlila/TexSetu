package com.texsetu.entity;

import com.texsetu.entity.enums.VerificationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "job_worker_profiles")
public class JobWorkerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @NotBlank
    @Column(name = "business_name", nullable = false, length = 150)
    private String businessName;

    @NotBlank
    @Column(name = "business_type", nullable = false, length = 80)
    private String businessType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotBlank
    @Column(nullable = false, length = 80)
    private String city = "Surat";

    @NotBlank
    @Column(name = "service_area", nullable = false, length = 150)
    private String serviceArea = "Surat & South Gujarat";

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(name = "machinery_details", columnDefinition = "TEXT")
    private String machineryDetails;

    @NotNull
    @Column(name = "indicative_capacity_meters_per_day", nullable = false, precision = 12, scale = 2)
    private BigDecimal indicativeCapacityMetersPerDay = BigDecimal.ZERO;

    @Column(name = "turnaround_days", nullable = false)
    private Integer turnaroundDays = 3;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 30)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(name = "verification_notes", columnDefinition = "TEXT")
    private String verificationNotes;

    @Column(name = "capacity_last_updated")
    private LocalDateTime capacityLastUpdated = LocalDateTime.now();

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @OneToMany(mappedBy = "jobWorker", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ServiceCapability> capabilities = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.capacityLastUpdated == null) {
            this.capacityLastUpdated = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public JobWorkerProfile() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getServiceArea() { return serviceArea; }
    public void setServiceArea(String serviceArea) { this.serviceArea = serviceArea; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getMachineryDetails() { return machineryDetails; }
    public void setMachineryDetails(String machineryDetails) { this.machineryDetails = machineryDetails; }

    public BigDecimal getIndicativeCapacityMetersPerDay() { return indicativeCapacityMetersPerDay; }
    public void setIndicativeCapacityMetersPerDay(BigDecimal indicativeCapacityMetersPerDay) { this.indicativeCapacityMetersPerDay = indicativeCapacityMetersPerDay; }

    public Integer getTurnaroundDays() { return turnaroundDays; }
    public void setTurnaroundDays(Integer turnaroundDays) { this.turnaroundDays = turnaroundDays; }

    public VerificationStatus getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(VerificationStatus verificationStatus) { this.verificationStatus = verificationStatus; }

    public String getVerificationNotes() { return verificationNotes; }
    public void setVerificationNotes(String verificationNotes) { this.verificationNotes = verificationNotes; }

    public LocalDateTime getCapacityLastUpdated() { return capacityLastUpdated; }
    public void setCapacityLastUpdated(LocalDateTime capacityLastUpdated) { this.capacityLastUpdated = capacityLastUpdated; }

    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; }

    public List<ServiceCapability> getCapabilities() { return capabilities; }
    public void setCapabilities(List<ServiceCapability> capabilities) { this.capabilities = capabilities; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
