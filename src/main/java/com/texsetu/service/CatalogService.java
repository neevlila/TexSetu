package com.texsetu.service;

import com.texsetu.entity.JobWorkerProfile;
import com.texsetu.entity.ServiceCapability;
import com.texsetu.entity.ServiceCategory;
import com.texsetu.entity.enums.UnitOfMeasure;
import com.texsetu.repository.JobWorkerProfileRepository;
import com.texsetu.repository.ServiceCapabilityRepository;
import com.texsetu.repository.ServiceCategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class CatalogService {

    private static final Logger log = LoggerFactory.getLogger(CatalogService.class);

    private final ServiceCategoryRepository serviceCategoryRepository;
    private final ServiceCapabilityRepository serviceCapabilityRepository;
    private final JobWorkerProfileRepository jobWorkerProfileRepository;

    public CatalogService(ServiceCategoryRepository serviceCategoryRepository,
                          ServiceCapabilityRepository serviceCapabilityRepository,
                          JobWorkerProfileRepository jobWorkerProfileRepository) {
        this.serviceCategoryRepository = serviceCategoryRepository;
        this.serviceCapabilityRepository = serviceCapabilityRepository;
        this.jobWorkerProfileRepository = jobWorkerProfileRepository;
    }

    @Transactional(readOnly = true)
    public List<ServiceCategory> getAllActiveCategories() {
        return serviceCategoryRepository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public Optional<ServiceCategory> getCategoryById(Long id) {
        return serviceCategoryRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<ServiceCapability> getCapabilitiesForWorker(Long workerId) {
        return serviceCapabilityRepository.findByJobWorkerId(workerId);
    }

    @Transactional(readOnly = true)
    public List<ServiceCapability> getActiveCapabilitiesForWorker(Long workerId) {
        return serviceCapabilityRepository.findByJobWorkerIdAndActiveTrue(workerId);
    }

    @Transactional
    public ServiceCapability addCapability(Long workerId, Long categoryId, String serviceName,
                                          String description, BigDecimal minQuantity,
                                          UnitOfMeasure uom, BigDecimal indicativePrice,
                                          Integer leadTimeDays, String processingDetails) {
        JobWorkerProfile worker = jobWorkerProfileRepository.findById(workerId)
                .orElseThrow(() -> new IllegalArgumentException("Job worker profile not found"));
        ServiceCategory category = serviceCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Service category not found"));

        if (minQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Minimum quantity must be greater than zero.");
        }
        if (indicativePrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero.");
        }

        ServiceCapability capability = new ServiceCapability();
        capability.setJobWorker(worker);
        capability.setCategory(category);
        capability.setServiceName(serviceName.trim());
        capability.setDescription(description);
        capability.setMinimumQuantity(minQuantity);
        capability.setUnitOfMeasure(uom);
        capability.setIndicativePricePerUnit(indicativePrice);
        capability.setLeadTimeDays(leadTimeDays != null && leadTimeDays > 0 ? leadTimeDays : 3);
        capability.setProcessingDetails(processingDetails);
        capability.setActive(true);

        ServiceCapability saved = serviceCapabilityRepository.save(capability);
        log.info("Added capability '{}' for worker '{}'", serviceName, worker.getBusinessName());
        return saved;
    }

    @Transactional
    public void deleteCapability(Long capabilityId, Long workerId) {
        ServiceCapability capability = serviceCapabilityRepository.findById(capabilityId)
                .orElseThrow(() -> new IllegalArgumentException("Capability not found"));
        if (!capability.getJobWorker().getId().equals(workerId)) {
            throw new SecurityException("Unauthorized capability modification.");
        }
        serviceCapabilityRepository.delete(capability);
        log.info("Deleted capability id {} for worker id {}", capabilityId, workerId);
    }
}
