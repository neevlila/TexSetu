package com.texsetu.service;

import com.texsetu.dto.QuotationSubmitDto;
import com.texsetu.dto.RfqCreateDto;
import com.texsetu.entity.*;
import com.texsetu.entity.enums.OrderStatus;
import com.texsetu.entity.enums.QuotationStatus;
import com.texsetu.entity.enums.RfqStatus;
import com.texsetu.notification.NotificationService;
import com.texsetu.repository.*;
import com.texsetu.storage.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RfqQuotationService {

    private static final Logger log = LoggerFactory.getLogger(RfqQuotationService.class);

    private final RfqRepository rfqRepository;
    private final RfqAttachmentRepository rfqAttachmentRepository;
    private final QuotationRepository quotationRepository;
    private final JobOrderRepository jobOrderRepository;
    private final ProductionLotRepository productionLotRepository;
    private final ProductionEventRepository productionEventRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;
    private final JobWorkerProfileRepository jobWorkerProfileRepository;
    private final FileStorageService fileStorageService;
    private final NotificationService notificationService;

    public RfqQuotationService(RfqRepository rfqRepository,
                               RfqAttachmentRepository rfqAttachmentRepository,
                               QuotationRepository quotationRepository,
                               JobOrderRepository jobOrderRepository,
                               ProductionLotRepository productionLotRepository,
                               ProductionEventRepository productionEventRepository,
                               ServiceCategoryRepository serviceCategoryRepository,
                               JobWorkerProfileRepository jobWorkerProfileRepository,
                               FileStorageService fileStorageService,
                               NotificationService notificationService) {
        this.rfqRepository = rfqRepository;
        this.rfqAttachmentRepository = rfqAttachmentRepository;
        this.quotationRepository = quotationRepository;
        this.jobOrderRepository = jobOrderRepository;
        this.productionLotRepository = productionLotRepository;
        this.productionEventRepository = productionEventRepository;
        this.serviceCategoryRepository = serviceCategoryRepository;
        this.jobWorkerProfileRepository = jobWorkerProfileRepository;
        this.fileStorageService = fileStorageService;
        this.notificationService = notificationService;
    }

    // --- RFQ METHODS ---

    @Transactional
    public Rfq createRfq(User buyer, RfqCreateDto dto) throws IOException {
        if (dto.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        if (dto.getDesiredCompletionDate().isBefore(LocalDate.now().plusDays(1))) {
            throw new IllegalArgumentException("Desired completion date must be in the future.");
        }

        ServiceCategory category = serviceCategoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid service category selected."));

        String rfqNumber = "RFQ-SRT-" + System.currentTimeMillis() % 1000000 + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        Rfq rfq = new Rfq();
        rfq.setRfqNumber(rfqNumber);
        rfq.setBuyer(buyer);
        rfq.setCategory(category);
        rfq.setTitle(dto.getTitle().trim());
        rfq.setFabricDescription(dto.getFabricDescription().trim());
        rfq.setQuantity(dto.getQuantity());
        rfq.setUnitOfMeasure(dto.getUnitOfMeasure());
        rfq.setDesiredCompletionDate(dto.getDesiredCompletionDate());
        rfq.setProcessingRequirements(dto.getProcessingRequirements().trim());
        rfq.setTechnicalSpecifications(dto.getTechnicalSpecifications() != null ? dto.getTechnicalSpecifications().trim() : null);
        rfq.setDeliveryRequirements(dto.getDeliveryRequirements() != null ? dto.getDeliveryRequirements().trim() : null);
        rfq.setStatus(RfqStatus.OPEN);

        Rfq savedRfq = rfqRepository.save(rfq);

        if (dto.getAttachment() != null && !dto.getAttachment().isEmpty()) {
            String fileUrl = fileStorageService.storeFile(dto.getAttachment(), "rfqs");
            RfqAttachment attachment = new RfqAttachment();
            attachment.setRfq(savedRfq);
            attachment.setFileName(dto.getAttachment().getOriginalFilename());
            attachment.setFileUrl(fileUrl);
            attachment.setFileType(dto.getAttachment().getContentType());
            attachment.setFileSize(dto.getAttachment().getSize());
            rfqAttachmentRepository.save(attachment);
        }

        log.info("Created RFQ {} by buyer {}", rfqNumber, buyer.getEmail());
        return savedRfq;
    }

    @Transactional(readOnly = true)
    public Optional<Rfq> getRfqById(Long id) {
        return rfqRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Rfq> getRfqsForBuyer(Long buyerId) {
        return rfqRepository.findByBuyerIdOrderByCreatedAtDesc(buyerId);
    }

    @Transactional(readOnly = true)
    public List<Rfq> getOpenRfqsForCategory(Long categoryId) {
        if (categoryId != null) {
            return rfqRepository.findByCategoryIdAndStatusOrderByCreatedAtDesc(categoryId, RfqStatus.OPEN);
        }
        return rfqRepository.findByStatusOrderByCreatedAtDesc(RfqStatus.OPEN);
    }

    // --- QUOTATION METHODS ---

    @Transactional
    public Quotation submitQuotation(JobWorkerProfile worker, QuotationSubmitDto dto) {
        Rfq rfq = rfqRepository.findById(dto.getRfqId())
                .orElseThrow(() -> new IllegalArgumentException("RFQ not found with ID: " + dto.getRfqId()));

        if (rfq.getStatus() != RfqStatus.OPEN && rfq.getStatus() != RfqStatus.QUOTED) {
            throw new IllegalStateException("Quotation cannot be submitted. RFQ status is: " + rfq.getStatus());
        }

        if (quotationRepository.existsByRfqIdAndJobWorkerId(rfq.getId(), worker.getId())) {
            throw new IllegalArgumentException("You have already submitted a quotation for this RFQ.");
        }

        if (dto.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0 || dto.getTotalPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Unit price and total price must be greater than zero.");
        }

        String quoteNumber = "QUO-" + System.currentTimeMillis() % 1000000 + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        Quotation quotation = new Quotation();
        quotation.setQuoteNumber(quoteNumber);
        quotation.setRfq(rfq);
        quotation.setJobWorker(worker);
        quotation.setUnitPrice(dto.getUnitPrice());
        quotation.setTotalPrice(dto.getTotalPrice());
        quotation.setMinQuantityConditions(dto.getMinQuantityConditions());
        quotation.setEstimatedDurationDays(dto.getEstimatedDurationDays());
        quotation.setProposedCompletionDate(dto.getProposedCompletionDate());
        quotation.setTermsConditions(dto.getTermsConditions());
        quotation.setValidityDate(dto.getValidityDate());
        quotation.setAdditionalNotes(dto.getAdditionalNotes());
        quotation.setStatus(QuotationStatus.SUBMITTED);

        Quotation savedQuote = quotationRepository.save(quotation);

        // Update RFQ status to QUOTED if still OPEN
        if (rfq.getStatus() == RfqStatus.OPEN) {
            rfq.setStatus(RfqStatus.QUOTED);
            rfqRepository.save(rfq);
        }

        // Send async notification to buyer
        notificationService.sendNotification(
                rfq.getBuyer(),
                "New Quotation Received: " + rfq.getRfqNumber(),
                "Provider " + worker.getBusinessName() + " submitted a quotation of INR " + dto.getTotalPrice() + " for your RFQ.",
                "/buyer/rfqs/" + rfq.getId() + "/quotes"
        );

        log.info("Quotation {} submitted by {} for RFQ {}", quoteNumber, worker.getBusinessName(), rfq.getRfqNumber());
        return savedQuote;
    }

    @Transactional(readOnly = true)
    public List<Quotation> getQuotationsForRfq(Long rfqId) {
        return quotationRepository.findByRfqIdOrderByTotalPriceAsc(rfqId);
    }

    @Transactional(readOnly = true)
    public List<Quotation> getQuotationsForWorker(Long workerId) {
        return quotationRepository.findByJobWorkerIdOrderByCreatedAtDesc(workerId);
    }

    // --- ATOMIC QUOTATION ACCEPTANCE ---

    @Transactional
    public JobOrder acceptQuotation(Long quotationId, User buyer) {
        Quotation selectedQuote = quotationRepository.findById(quotationId)
                .orElseThrow(() -> new IllegalArgumentException("Quotation not found with ID: " + quotationId));

        Rfq rfq = selectedQuote.getRfq();

        // Security check: Only the buyer who owns the RFQ can accept a quote
        if (!rfq.getBuyer().getId().equals(buyer.getId())) {
            throw new SecurityException("Unauthorized: You do not own this RFQ.");
        }

        // Concurrency check: RFQ must still be in OPEN or QUOTED state
        if (rfq.getStatus() != RfqStatus.OPEN && rfq.getStatus() != RfqStatus.QUOTED) {
            throw new IllegalStateException("Quotation acceptance rejected: RFQ is already " + rfq.getStatus());
        }

        if (selectedQuote.getStatus() != QuotationStatus.SUBMITTED) {
            throw new IllegalStateException("Quotation is not in SUBMITTED state. Current state: " + selectedQuote.getStatus());
        }

        // 1. Mark RFQ as CLOSED
        rfq.setStatus(RfqStatus.CLOSED);
        rfqRepository.save(rfq);

        // 2. Mark selected quotation as ACCEPTED
        selectedQuote.setStatus(QuotationStatus.ACCEPTED);
        quotationRepository.save(selectedQuote);

        // 3. Mark all other quotations for this RFQ as REJECTED
        List<Quotation> otherQuotes = quotationRepository.findByRfqIdOrderByTotalPriceAsc(rfq.getId());
        for (Quotation other : otherQuotes) {
            if (!other.getId().equals(selectedQuote.getId()) && other.getStatus() == QuotationStatus.SUBMITTED) {
                other.setStatus(QuotationStatus.REJECTED);
                quotationRepository.save(other);
                notificationService.sendNotification(
                        other.getJobWorker().getUser(),
                        "Quotation Update for RFQ: " + rfq.getRfqNumber(),
                        "Another quotation was selected by the buyer for RFQ " + rfq.getRfqNumber() + ".",
                        "/job-worker/quotes"
                );
            }
        }

        // 4. Create JobOrder
        String orderNumber = "ORD-SRT-" + System.currentTimeMillis() % 1000000 + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        JobOrder order = new JobOrder();
        order.setOrderNumber(orderNumber);
        order.setQuotation(selectedQuote);
        order.setRfq(rfq);
        order.setBuyer(buyer);
        order.setJobWorker(selectedQuote.getJobWorker());
        order.setCategory(rfq.getCategory());
        order.setAgreedQuantity(rfq.getQuantity());
        order.setUnitOfMeasure(rfq.getUnitOfMeasure());
        order.setAgreedPrice(selectedQuote.getTotalPrice());
        order.setExpectedCompletionDate(selectedQuote.getProposedCompletionDate());
        order.setCurrentStatus(OrderStatus.CONFIRMED);

        JobOrder savedOrder = jobOrderRepository.save(order);

        // 5. Initialize the signature Digital Production Lot Passport
        String lotNumber = "LOT-SRT-" + System.currentTimeMillis() % 1000000 + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        ProductionLot lot = new ProductionLot();
        lot.setLotNumber(lotNumber);
        lot.setOrder(savedOrder);
        lot.setOriginalSubmittedQuantity(rfq.getQuantity());
        lot.setReceivedQuantity(rfq.getQuantity()); // Initialized with submitted quantity, verified on material receipt
        lot.setProcessingCategory(rfq.getCategory().getName());
        lot.setProcessingNotes("Lot generated upon acceptance of Quotation " + selectedQuote.getQuoteNumber());
        lot.setCurrentStage("LOT_INITIALIZED");
        lot.setQuantityCompleted(BigDecimal.ZERO);
        lot.setQuantityRejectedWaste(BigDecimal.ZERO);
        lot.setStatus("ACTIVE");

        ProductionLot savedLot = productionLotRepository.save(lot);

        // 6. Record the first immutable event in the Production Event Ledger
        ProductionEvent initialEvent = new ProductionEvent(
                savedLot,
                "ORDER_CONFIRMED",
                null,
                OrderStatus.CONFIRMED.name(),
                buyer,
                "BUYER",
                "Job order created upon acceptance of Quotation " + selectedQuote.getQuoteNumber() + ". Production Lot Passport initialized.",
                null
        );
        productionEventRepository.save(initialEvent);

        // 7. Notify selected Job-Worker
        notificationService.sendNotification(
                selectedQuote.getJobWorker().getUser(),
                "Quotation Accepted! New Order: " + orderNumber,
                "Congratulations! Buyer " + buyer.getFullName() + " accepted your quotation of INR " + selectedQuote.getTotalPrice() + ".",
                "/job-worker/orders/" + savedOrder.getId()
        );

        log.info("Quotation {} accepted. Job order {} and lot {} created atomically.",
                selectedQuote.getQuoteNumber(), orderNumber, lotNumber);

        return savedOrder;
    }
}
