package com.texsetu.service;

import com.texsetu.dto.InspectionCreateDto;
import com.texsetu.entity.*;
import com.texsetu.entity.enums.InspectionResult;
import com.texsetu.entity.enums.OrderStatus;
import com.texsetu.notification.NotificationService;
import com.texsetu.repository.JobOrderRepository;
import com.texsetu.repository.ProductionEventRepository;
import com.texsetu.repository.ProductionLotRepository;
import com.texsetu.repository.QualityInspectionRepository;
import com.texsetu.storage.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@Service
public class QualityInspectionService {

    private static final Logger log = LoggerFactory.getLogger(QualityInspectionService.class);

    private final QualityInspectionRepository qualityInspectionRepository;
    private final JobOrderRepository jobOrderRepository;
    private final ProductionLotRepository productionLotRepository;
    private final ProductionEventRepository productionEventRepository;
    private final FileStorageService fileStorageService;
    private final NotificationService notificationService;

    public QualityInspectionService(QualityInspectionRepository qualityInspectionRepository,
                                  JobOrderRepository jobOrderRepository,
                                  ProductionLotRepository productionLotRepository,
                                  ProductionEventRepository productionEventRepository,
                                  FileStorageService fileStorageService,
                                  NotificationService notificationService) {
        this.qualityInspectionRepository = qualityInspectionRepository;
        this.jobOrderRepository = jobOrderRepository;
        this.productionLotRepository = productionLotRepository;
        this.productionEventRepository = productionEventRepository;
        this.fileStorageService = fileStorageService;
        this.notificationService = notificationService;
    }

    @Transactional
    public QualityInspection recordInspection(User inspector, InspectionCreateDto dto) throws IOException {
        JobOrder order = jobOrderRepository.findById(dto.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Job order not found with ID: " + dto.getOrderId()));

        // Verification: Only buyer, assigned inspector, or admin can submit inspection
        if (!order.getBuyer().getId().equals(inspector.getId()) && !inspector.getRole().name().equals("ADMIN")) {
            throw new SecurityException("Unauthorized: Only the buyer or an admin can conduct quality inspection.");
        }

        ProductionLot lot = productionLotRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new IllegalStateException("Production lot missing for order"));

        if (dto.getQuantityInspected().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Inspected quantity must be positive.");
        }
        if (dto.getAcceptedQuantity().add(dto.getRejectedQuantity() != null ? dto.getRejectedQuantity() : BigDecimal.ZERO)
                .compareTo(dto.getQuantityInspected()) > 0) {
            throw new IllegalArgumentException("Sum of accepted and rejected quantity cannot exceed total inspected quantity.");
        }

        String photoUrl = null;
        if (dto.getEvidencePhoto() != null && !dto.getEvidencePhoto().isEmpty()) {
            photoUrl = fileStorageService.storeFile(dto.getEvidencePhoto(), "inspections");
        }

        QualityInspection inspection = new QualityInspection();
        inspection.setProductionLot(lot);
        inspection.setOrder(order);
        inspection.setInspector(inspector);
        inspection.setInspectorRole(inspector.getRole().name());
        inspection.setQuantityInspected(dto.getQuantityInspected());
        inspection.setAcceptedQuantity(dto.getAcceptedQuantity());
        inspection.setRejectedQuantity(dto.getRejectedQuantity() != null ? dto.getRejectedQuantity() : BigDecimal.ZERO);
        inspection.setDefectDetails(dto.getDefectDetails());
        inspection.setInspectionComments(dto.getInspectionComments().trim());
        inspection.setResult(dto.getResult());
        inspection.setEvidencePhotoUrl(photoUrl);

        QualityInspection savedInspection = qualityInspectionRepository.save(inspection);

        // Process Result Workflow
        if (dto.getResult() == InspectionResult.ACCEPTED) {
            order.setCurrentStatus(OrderStatus.COMPLETED);
            jobOrderRepository.save(order);

            lot.setCurrentStage("QUALITY_PASSED_COMPLETED");
            lot.setQuantityCompleted(dto.getAcceptedQuantity());
            lot.setQuantityRejectedWaste(dto.getRejectedQuantity() != null ? dto.getRejectedQuantity() : BigDecimal.ZERO);
            lot.setStatus("CLOSED");
            productionLotRepository.save(lot);

            ProductionEvent event = new ProductionEvent(
                    lot,
                    "QUALITY_ACCEPTED",
                    OrderStatus.QUALITY_CHECK.name(),
                    OrderStatus.COMPLETED.name(),
                    inspector,
                    inspector.getRole().name(),
                    "Quality inspection APPROVED. Accepted: " + dto.getAcceptedQuantity() + " " + order.getUnitOfMeasure() + ". Comments: " + dto.getInspectionComments(),
                    photoUrl
            );
            productionEventRepository.save(event);

            notificationService.sendNotification(
                    order.getJobWorker().getUser(),
                    "Quality Inspection Passed: " + order.getOrderNumber(),
                    "Buyer approved the lot quality. Order is now marked as COMPLETED and ready for dispatch.",
                    "/job-worker/orders/" + order.getId()
            );

        } else if (dto.getResult() == InspectionResult.REWORK_REQUIRED) {
            // Rework required: Revert order back to PROCESSING
            order.setCurrentStatus(OrderStatus.PROCESSING);
            jobOrderRepository.save(order);

            lot.setCurrentStage("REWORK_IN_PROGRESS");
            lot.setStatus("REWORK");
            productionLotRepository.save(lot);

            ProductionEvent event = new ProductionEvent(
                    lot,
                    "QUALITY_REWORK_REQUESTED",
                    OrderStatus.QUALITY_CHECK.name(),
                    OrderStatus.PROCESSING.name(),
                    inspector,
                    inspector.getRole().name(),
                    "Rework requested by buyer. Defects: " + dto.getDefectDetails() + ". Comments: " + dto.getInspectionComments(),
                    photoUrl
            );
            productionEventRepository.save(event);

            notificationService.sendNotification(
                    order.getJobWorker().getUser(),
                    "Action Required: Rework Requested for Order " + order.getOrderNumber(),
                    "Buyer identified quality issues and requested rework: " + dto.getDefectDetails(),
                    "/job-worker/orders/" + order.getId()
            );

        } else if (dto.getResult() == InspectionResult.REJECTED) {
            lot.setCurrentStage("LOT_REJECTED");
            lot.setStatus("REJECTED");
            productionLotRepository.save(lot);

            ProductionEvent event = new ProductionEvent(
                    lot,
                    "QUALITY_REJECTED",
                    OrderStatus.QUALITY_CHECK.name(),
                    "LOT_REJECTED",
                    inspector,
                    inspector.getRole().name(),
                    "Lot quality REJECTED by inspector. Defects: " + dto.getDefectDetails(),
                    photoUrl
            );
            productionEventRepository.save(event);

            notificationService.sendNotification(
                    order.getJobWorker().getUser(),
                    "Quality Inspection Rejected: " + order.getOrderNumber(),
                    "The lot has been rejected due to critical defects: " + dto.getDefectDetails(),
                    "/job-worker/orders/" + order.getId()
            );
        }

        log.info("Recorded quality inspection for Order {}: Result={}", order.getOrderNumber(), dto.getResult());
        return savedInspection;
    }

    @Transactional(readOnly = true)
    public List<QualityInspection> getInspectionsForOrder(Long orderId) {
        return qualityInspectionRepository.findByOrderIdOrderByInspectionDateDesc(orderId);
    }
}
