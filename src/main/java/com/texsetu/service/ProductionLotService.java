package com.texsetu.service;

import com.texsetu.entity.JobOrder;
import com.texsetu.entity.ProductionEvent;
import com.texsetu.entity.ProductionLot;
import com.texsetu.entity.User;
import com.texsetu.repository.JobOrderRepository;
import com.texsetu.repository.ProductionEventRepository;
import com.texsetu.repository.ProductionLotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ProductionLotService {

    private static final Logger log = LoggerFactory.getLogger(ProductionLotService.class);

    private final ProductionLotRepository productionLotRepository;
    private final ProductionEventRepository productionEventRepository;
    private final JobOrderRepository jobOrderRepository;

    public ProductionLotService(ProductionLotRepository productionLotRepository,
                                ProductionEventRepository productionEventRepository,
                                JobOrderRepository jobOrderRepository) {
        this.productionLotRepository = productionLotRepository;
        this.productionEventRepository = productionEventRepository;
        this.jobOrderRepository = jobOrderRepository;
    }

    @Transactional(readOnly = true)
    public Optional<ProductionLot> getLotByOrderId(Long orderId) {
        return productionLotRepository.findByOrderId(orderId);
    }

    @Transactional(readOnly = true)
    public Optional<ProductionLot> getLotByLotNumber(String lotNumber) {
        return productionLotRepository.findByLotNumber(lotNumber);
    }

    @Transactional(readOnly = true)
    public List<ProductionEvent> getTimelineForLot(Long lotId) {
        return productionEventRepository.findByProductionLotIdOrderByEventTimestampAsc(lotId);
    }

    @Transactional
    public ProductionLot reconcileReceivedMaterial(Long lotId, BigDecimal receivedQuantity, String notes, User worker) {
        ProductionLot lot = productionLotRepository.findById(lotId)
                .orElseThrow(() -> new IllegalArgumentException("Production lot not found"));

        JobOrder order = lot.getOrder();
        if (!order.getJobWorker().getUser().getId().equals(worker.getId())) {
            throw new SecurityException("Unauthorized: Only the assigned job worker can record material receipt.");
        }

        if (receivedQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Received quantity must be positive.");
        }

        lot.setReceivedQuantity(receivedQuantity);
        lot.setCurrentStage("MATERIAL_RECEIVED_VERIFIED");
        lot.setProcessingNotes(notes != null ? notes.trim() : "Material received and verified at factory unit.");
        ProductionLot savedLot = productionLotRepository.save(lot);

        ProductionEvent event = new ProductionEvent(
                savedLot,
                "MATERIAL_RECONCILED",
                null,
                "MATERIAL_VERIFIED",
                worker,
                "JOB_WORKER",
                "Physical material received at unit. Submitted: " + lot.getOriginalSubmittedQuantity() + " " + order.getUnitOfMeasure() + ", Verified Received: " + receivedQuantity + " " + order.getUnitOfMeasure() + ". Note: " + notes,
                null
        );
        productionEventRepository.save(event);

        log.info("Material reconciled for Lot {}: received={}", lot.getLotNumber(), receivedQuantity);
        return savedLot;
    }
}
