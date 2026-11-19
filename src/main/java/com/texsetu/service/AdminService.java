package com.texsetu.service;

import com.texsetu.entity.JobWorkerProfile;
import com.texsetu.entity.ServiceCategory;
import com.texsetu.entity.User;
import com.texsetu.entity.VerificationRecord;
import com.texsetu.entity.enums.AccountStatus;
import com.texsetu.entity.enums.OrderStatus;
import com.texsetu.entity.enums.Role;
import com.texsetu.entity.enums.VerificationStatus;
import com.texsetu.notification.NotificationService;
import com.texsetu.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);

    private final UserRepository userRepository;
    private final JobWorkerProfileRepository jobWorkerProfileRepository;
    private final RfqRepository rfqRepository;
    private final QuotationRepository quotationRepository;
    private final JobOrderRepository jobOrderRepository;
    private final ComplaintRepository complaintRepository;
    private final VerificationRecordRepository verificationRecordRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;
    private final NotificationService notificationService;

    public AdminService(UserRepository userRepository,
                        JobWorkerProfileRepository jobWorkerProfileRepository,
                        RfqRepository rfqRepository,
                        QuotationRepository quotationRepository,
                        JobOrderRepository jobOrderRepository,
                        ComplaintRepository complaintRepository,
                        VerificationRecordRepository verificationRecordRepository,
                        ServiceCategoryRepository serviceCategoryRepository,
                        NotificationService notificationService) {
        this.userRepository = userRepository;
        this.jobWorkerProfileRepository = jobWorkerProfileRepository;
        this.rfqRepository = rfqRepository;
        this.quotationRepository = quotationRepository;
        this.jobOrderRepository = jobOrderRepository;
        this.complaintRepository = complaintRepository;
        this.verificationRecordRepository = verificationRecordRepository;
        this.serviceCategoryRepository = serviceCategoryRepository;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAdminDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("buyerCount", userRepository.countByRole(Role.BUYER));
        stats.put("workerCount", userRepository.countByRole(Role.JOB_WORKER));
        stats.put("pendingVerifications", jobWorkerProfileRepository.countByVerificationStatus(VerificationStatus.PENDING));
        stats.put("activeRfqs", rfqRepository.count());
        stats.put("totalQuotations", quotationRepository.count());
        stats.put("activeOrders", jobOrderRepository.countByCurrentStatus(OrderStatus.PROCESSING)
                + jobOrderRepository.countByCurrentStatus(OrderStatus.CONFIRMED)
                + jobOrderRepository.countByCurrentStatus(OrderStatus.MATERIAL_RECEIVED)
                + jobOrderRepository.countByCurrentStatus(OrderStatus.QUALITY_CHECK));
        stats.put("completedOrders", jobOrderRepository.countByCurrentStatus(OrderStatus.COMPLETED)
                + jobOrderRepository.countByCurrentStatus(OrderStatus.DELIVERED));
        stats.put("openComplaints", complaintRepository.count());
        return stats;
    }

    @Transactional
    public void processProviderVerification(Long workerId, User admin, VerificationStatus newStatus, String reviewNotes) {
        JobWorkerProfile worker = jobWorkerProfileRepository.findById(workerId)
                .orElseThrow(() -> new IllegalArgumentException("Worker profile not found"));

        worker.setVerificationStatus(newStatus);
        worker.setVerificationNotes(reviewNotes != null ? reviewNotes.trim() : null);
        if (newStatus == VerificationStatus.VERIFIED) {
            worker.setVerifiedAt(LocalDateTime.now());
        }
        jobWorkerProfileRepository.save(worker);

        VerificationRecord record = new VerificationRecord();
        record.setJobWorker(worker);
        record.setAdmin(admin);
        record.setStatus(newStatus);
        record.setReviewNotes(reviewNotes != null ? reviewNotes.trim() : null);
        record.setReviewedAt(LocalDateTime.now());
        verificationRecordRepository.save(record);

        notificationService.sendNotification(
                worker.getUser(),
                "Provider Verification Status: " + newStatus,
                "Your provider verification status has been updated to " + newStatus + ". Notes: " + reviewNotes,
                "/job-worker/profile"
        );

        log.info("Admin {} updated provider verification for {} to {}", admin.getEmail(), worker.getBusinessName(), newStatus);
    }

    @Transactional
    public void toggleUserAccountStatus(Long userId, AccountStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setAccountStatus(status);
        userRepository.save(user);
        log.info("Toggled account status for user {} to {}", user.getEmail(), status);
    }

    @Transactional
    public ServiceCategory createOrUpdateCategory(Long id, String code, String name, String description, Boolean active) {
        ServiceCategory category;
        if (id != null) {
            category = serviceCategoryRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        } else {
            category = new ServiceCategory();
            category.setCode(code.trim().toUpperCase());
        }
        category.setName(name.trim());
        category.setDescription(description != null ? description.trim() : null);
        category.setActive(active != null ? active : true);
        return serviceCategoryRepository.save(category);
    }

    @Transactional(readOnly = true)
    public List<JobWorkerProfile> getPendingVerifications() {
        return jobWorkerProfileRepository.findByVerificationStatus(VerificationStatus.PENDING);
    }
}
