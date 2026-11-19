package com.texsetu.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "rfq_attachments")
public class RfqAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rfq_id", nullable = false)
    private Rfq rfq;

    @NotBlank
    @Column(name = "file_name", nullable = false)
    private String fileName;

    @NotBlank
    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;

    @NotBlank
    @Column(name = "file_type", nullable = false, length = 100)
    private String fileType;

    @NotNull
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        this.uploadedAt = LocalDateTime.now();
    }

    public RfqAttachment() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Rfq getRfq() { return rfq; }
    public void setRfq(Rfq rfq) { this.rfq = rfq; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}
