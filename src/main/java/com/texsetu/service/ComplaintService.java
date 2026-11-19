package com.texsetu.service;

import com.texsetu.dto.ComplaintCreateDto;
import com.texsetu.entity.Complaint;
import com.texsetu.entity.JobOrder;
import com.texsetu.entity.User;
import com.texsetu.entity.enums.ComplaintStatus;
import com.texsetu.notification.NotificationService;
import com.texsetu.repository.ComplaintRepository;
import com.texsetu.repository.JobOrderRepository;
import com.texsetu.storage.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ComplaintService {

    private static final Logger log = LoggerFactory.getLogger(ComplaintService.class);

    private final ComplaintRepository complaintRepository;
    private final JobOrderRepository jobOrderRepository;
    private final FileStorageService fileStorageService;
    private final NotificationService notificationService;

    public ComplaintService(ComplaintRepository complaintRepository,
                            JobOrderRepository jobOrderRepository,
                            FileStorageService fileStorageService,
                            NotificationService notificationService) {
        this.complaintRepository = complaintRepository;
        this.jobOrderRepository = jobOrderRepository;
        this.fileStorageService = fileStorageService;
        this.notificationService = notificationService;
    }

    @Transactional
    public Complaint lodgeComplaint(User complainant, ComplaintCreateDto dto) throws IOException {
        JobOrder order = jobOrderRepository.findById(dto.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Job order not found with ID: " + dto.getOrderId()));

        // Verification: Complainant must be associated with the order (buyer or worker)
        boolean isBuyer = order.getBuyer().getId().equals(complainant.getId());
        boolean isWorker = order.getJobWorker().getUser().getId().equals(complainant.getId());

        if (!isBuyer && !isWorker) {
            throw new SecurityException("Unauthorized: You are not a party to this order.");
        }

        String evidenceUrl = null;
        if (dto.getEvidenceFile() != null && !dto.getEvidenceFile().isEmpty()) {
            evidenceUrl = fileStorageService.storeFile(dto.getEvidenceFile(), "complaints");
        }

        String complaintRef = "CMP-SRT-" + System.currentTimeMillis() % 1000000 + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        Complaint complaint = new Complaint();
        complaint.setComplaintReference(complaintRef);
        complaint.setOrder(order);
        complaint.setComplainant(complainant);
        complaint.setCategory(dto.getCategory().trim());
        complaint.setDescription(dto.getDescription().trim());
        complaint.setEvidenceUrl(evidenceUrl);
        complaint.setStatus(ComplaintStatus.OPEN);

        Complaint saved = complaintRepository.save(complaint);
        log.info("Complaint logged: Ref={} for Order={}", complaintRef, order.getOrderNumber());
        return saved;
    }

    @Transactional
    public Complaint resolveComplaint(Long complaintId, ComplaintStatus newStatus, String adminNotes, String resolutionNotes) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found"));

        complaint.setStatus(newStatus);
        complaint.setAdminNotes(adminNotes != null ? adminNotes.trim() : null);
        complaint.setResolutionNotes(resolutionNotes != null ? resolutionNotes.trim() : null);
        if (newStatus == ComplaintStatus.RESOLVED || newStatus == ComplaintStatus.REJECTED) {
            complaint.setResolvedAt(LocalDateTime.now());
        }

        Complaint saved = complaintRepository.save(complaint);

        // Notify complainant
        notificationService.sendNotification(
                complaint.getComplainant(),
                "Complaint Update: " + complaint.getComplaintReference(),
                "Status updated to " + newStatus + ". Resolution Notes: " + resolutionNotes,
                "/buyer/complaints"
        );

        log.info("Complaint {} updated to status {}", complaint.getComplaintReference(), newStatus);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Complaint> getComplaintsForUser(Long userId) {
        return complaintRepository.findByComplainantIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<Complaint> getAllComplaints() {
        return complaintRepository.findAll();
    }
}
