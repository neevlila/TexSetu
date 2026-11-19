package com.texsetu.service;

import com.texsetu.dto.ProductionUpdateDto;
import com.texsetu.entity.*;
import com.texsetu.entity.enums.OrderStatus;
import com.texsetu.entity.enums.Role;
import com.texsetu.notification.NotificationService;
import com.texsetu.repository.JobOrderRepository;
import com.texsetu.repository.ProductionEventRepository;
import com.texsetu.repository.ProductionLotRepository;
import com.texsetu.storage.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class JobOrderService {

    private static final Logger log = LoggerFactory.getLogger(JobOrderService.class);

    private final JobOrderRepository jobOrderRepository;
    private final ProductionLotRepository productionLotRepository;
    private final ProductionEventRepository productionEventRepository;
    private final FileStorageService fileStorageService;
    private final NotificationService notificationService;

    public JobOrderService(JobOrderRepository jobOrderRepository,
                           ProductionLotRepository productionLotRepository,
                           ProductionEventRepository productionEventRepository,
                           FileStorageService fileStorageService,
                           NotificationService notificationService) {
        this.jobOrderRepository = jobOrderRepository;
        this.productionLotRepository = productionLotRepository;
        this.productionEventRepository = productionEventRepository;
        this.fileStorageService = fileStorageService;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public Optional<JobOrder> getOrderById(Long id) {
        return jobOrderRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<JobOrder> getOrdersForBuyer(Long buyerId) {
        return jobOrderRepository.findByBuyerIdOrderByCreatedAtDesc(buyerId);
    }

    @Transactional(readOnly = true)
    public List<JobOrder> getOrdersForWorker(Long workerId) {
        return jobOrderRepository.findByJobWorkerIdOrderByCreatedAtDesc(workerId);
    }

    @Transactional
    public JobOrder updateOrderStatus(User actor, ProductionUpdateDto dto) throws IOException {
        JobOrder order = jobOrderRepository.findById(dto.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Job order not found with ID: " + dto.getOrderId()));

        OrderStatus currentStatus = order.getCurrentStatus();
        OrderStatus nextStatus = dto.getNextStatus();

        // 1. Strict State Machine Transition Validation
        if (!currentStatus.canTransitionTo(nextStatus)) {
            throw new IllegalArgumentException("Invalid status transition: Cannot move from " + currentStatus + " to " + nextStatus);
        }

        // 2. Role-based Permission Validation
        if (actor.getRole() == Role.JOB_WORKER) {
            if (!order.getJobWorker().getUser().getId().equals(actor.getId())) {
                throw new SecurityException("Unauthorized: You are not the assigned job worker for this order.");
            }
            if (nextStatus == OrderStatus.DELIVERED) {
                throw new SecurityException("Only the buyer can confirm final delivery receipt.");
            }
        } else if (actor.getRole() == Role.BUYER) {
            if (!order.getBuyer().getId().equals(actor.getId())) {
                throw new SecurityException("Unauthorized: You do not own this order.");
            }
            if (nextStatus != OrderStatus.DELIVERED && nextStatus != OrderStatus.CANCELLED) {
                throw new SecurityException("Buyers can only confirm final delivery or cancel under allowed terms.");
            }
        }

        // 3. Handle File Attachment
        String attachmentUrl = null;
        if (dto.getAttachment() != null && !dto.getAttachment().isEmpty()) {
            attachmentUrl = fileStorageService.storeFile(dto.getAttachment(), "production");
        }

        // 4. Update Order Status
        order.setCurrentStatus(nextStatus);
        JobOrder savedOrder = jobOrderRepository.save(order);

        // 5. Update Production Lot Passport
        ProductionLot lot = productionLotRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new IllegalStateException("Production lot passport missing for order: " + order.getOrderNumber()));

        lot.setCurrentStage(dto.getStageName().trim());
        if (dto.getCompletedQuantity() != null && dto.getCompletedQuantity().compareTo(BigDecimal.ZERO) >= 0) {
            lot.setQuantityCompleted(dto.getCompletedQuantity());
        }
        if (dto.getRejectedWasteQuantity() != null && dto.getRejectedWasteQuantity().compareTo(BigDecimal.ZERO) >= 0) {
            lot.setQuantityRejectedWaste(dto.getRejectedWasteQuantity());
        }
        if (nextStatus == OrderStatus.COMPLETED || nextStatus == OrderStatus.DELIVERED) {
            lot.setStatus("CLOSED");
        }
        productionLotRepository.save(lot);

        // 6. Record Immutable Event into Passport Ledger
        ProductionEvent event = new ProductionEvent(
                lot,
                "STATUS_CHANGE_" + nextStatus.name(),
                currentStatus.name(),
                nextStatus.name(),
                actor,
                actor.getRole().name(),
                dto.getDescription().trim(),
                attachmentUrl
        );
        productionEventRepository.save(event);

        // 7. Dispatch Notification to Counterparty
        User recipient = actor.getRole() == Role.JOB_WORKER ? order.getBuyer() : order.getJobWorker().getUser();
        String targetUrl = actor.getRole() == Role.JOB_WORKER ?
                "/buyer/orders/" + order.getId() : "/job-worker/orders/" + order.getId();

        notificationService.sendNotification(
                recipient,
                "Order Update: " + order.getOrderNumber(),
                "Order status progressed to " + nextStatus + ": " + dto.getDescription(),
                targetUrl
        );

        log.info("Order {} transitioned from {} to {} by {}", order.getOrderNumber(), currentStatus, nextStatus, actor.getEmail());
        return savedOrder;
    }
}
