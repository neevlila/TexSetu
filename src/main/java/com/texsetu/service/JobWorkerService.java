package com.texsetu.service;

import com.texsetu.entity.JobWorkerProfile;
import com.texsetu.entity.User;
import com.texsetu.entity.enums.OrderStatus;
import com.texsetu.entity.enums.QuotationStatus;
import com.texsetu.entity.enums.VerificationStatus;
import com.texsetu.repository.JobOrderRepository;
import com.texsetu.repository.JobWorkerProfileRepository;
import com.texsetu.repository.QuotationRepository;
import com.texsetu.repository.RfqRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class JobWorkerService {

    private static final Logger log = LoggerFactory.getLogger(JobWorkerService.class);

    private final JobWorkerProfileRepository jobWorkerProfileRepository;
    private final QuotationRepository quotationRepository;
    private final JobOrderRepository jobOrderRepository;
    private final RfqRepository rfqRepository;

    public JobWorkerService(JobWorkerProfileRepository jobWorkerProfileRepository,
                            QuotationRepository quotationRepository,
                            JobOrderRepository jobOrderRepository,
                            RfqRepository rfqRepository) {
        this.jobWorkerProfileRepository = jobWorkerProfileRepository;
        this.quotationRepository = quotationRepository;
        this.jobOrderRepository = jobOrderRepository;
        this.rfqRepository = rfqRepository;
    }

    @Transactional(readOnly = true)
    public Optional<JobWorkerProfile> getProfileByUserId(Long userId) {
        return jobWorkerProfileRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Optional<JobWorkerProfile> getProfileById(Long id) {
        return jobWorkerProfileRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<JobWorkerProfile> getAllWorkers() {
        return jobWorkerProfileRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getWorkerDashboardStats(Long workerId) {
        Map<String, Object> stats = new HashMap<>();
        long totalQuotes = quotationRepository.countByJobWorkerId(workerId);
        long pendingQuotes = quotationRepository.countByJobWorkerIdAndStatus(workerId, QuotationStatus.SUBMITTED);
        long acceptedOrders = quotationRepository.countByJobWorkerIdAndStatus(workerId, QuotationStatus.ACCEPTED);
        long activeOrders = jobOrderRepository.countByJobWorkerIdAndCurrentStatus(workerId, OrderStatus.PROCESSING)
                + jobOrderRepository.countByJobWorkerIdAndCurrentStatus(workerId, OrderStatus.CONFIRMED)
                + jobOrderRepository.countByJobWorkerIdAndCurrentStatus(workerId, OrderStatus.MATERIAL_RECEIVED)
                + jobOrderRepository.countByJobWorkerIdAndCurrentStatus(workerId, OrderStatus.QUALITY_CHECK);
        long completedOrders = jobOrderRepository.countByJobWorkerIdAndCurrentStatus(workerId, OrderStatus.COMPLETED)
                + jobOrderRepository.countByJobWorkerIdAndCurrentStatus(workerId, OrderStatus.DELIVERED);

        stats.put("totalQuotes", totalQuotes);
        stats.put("pendingQuotes", pendingQuotes);
        stats.put("acceptedOrders", acceptedOrders);
        stats.put("activeOrders", activeOrders);
        stats.put("completedOrders", completedOrders);
        return stats;
    }

    @Transactional
    public JobWorkerProfile updateCapacityAndMachinery(Long workerId, BigDecimal capacityMetersPerDay, Integer turnaroundDays, String machineryDetails) {
        JobWorkerProfile profile = jobWorkerProfileRepository.findById(workerId)
                .orElseThrow(() -> new IllegalArgumentException("Worker profile not found"));

        if (capacityMetersPerDay != null && capacityMetersPerDay.compareTo(BigDecimal.ZERO) >= 0) {
            profile.setIndicativeCapacityMetersPerDay(capacityMetersPerDay);
        }
        if (turnaroundDays != null && turnaroundDays > 0) {
            profile.setTurnaroundDays(turnaroundDays);
        }
        if (machineryDetails != null) {
            profile.setMachineryDetails(machineryDetails.trim());
        }
        profile.setCapacityLastUpdated(LocalDateTime.now());

        JobWorkerProfile updated = jobWorkerProfileRepository.save(profile);
        log.info("Updated capacity for worker id {}: capacity={}, turnaround={}", workerId, capacityMetersPerDay, turnaroundDays);
        return updated;
    }

    @Transactional
    public void submitVerificationRequest(Long workerId, String notes) {
        JobWorkerProfile profile = jobWorkerProfileRepository.findById(workerId)
                .orElseThrow(() -> new IllegalArgumentException("Worker profile not found"));

        profile.setVerificationStatus(VerificationStatus.PENDING);
        profile.setVerificationNotes(notes != null ? notes.trim() : "Verification requested by provider.");
        jobWorkerProfileRepository.save(profile);
        log.info("Verification request submitted for worker id {}", workerId);
    }
}
