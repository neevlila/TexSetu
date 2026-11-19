package com.texsetu;

import com.texsetu.dto.QuotationSubmitDto;
import com.texsetu.dto.RfqCreateDto;
import com.texsetu.entity.*;
import com.texsetu.entity.enums.OrderStatus;
import com.texsetu.entity.enums.QuotationStatus;
import com.texsetu.entity.enums.RfqStatus;
import com.texsetu.entity.enums.Role;
import com.texsetu.entity.enums.UnitOfMeasure;
import com.texsetu.notification.NotificationService;
import com.texsetu.repository.*;
import com.texsetu.service.RfqQuotationService;
import com.texsetu.storage.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RfqQuotationConcurrencyTest {

    @Mock private RfqRepository rfqRepository;
    @Mock private RfqAttachmentRepository rfqAttachmentRepository;
    @Mock private QuotationRepository quotationRepository;
    @Mock private JobOrderRepository jobOrderRepository;
    @Mock private ProductionLotRepository productionLotRepository;
    @Mock private ProductionEventRepository productionEventRepository;
    @Mock private ServiceCategoryRepository serviceCategoryRepository;
    @Mock private JobWorkerProfileRepository jobWorkerProfileRepository;
    @Mock private FileStorageService fileStorageService;
    @Mock private NotificationService notificationService;

    private RfqQuotationService service;

    private User buyer;
    private User otherBuyer;
    private JobWorkerProfile worker1;
    private JobWorkerProfile worker2;
    private ServiceCategory category;

    @BeforeEach
    void setUp() {
        service = new RfqQuotationService(
                rfqRepository, rfqAttachmentRepository, quotationRepository,
                jobOrderRepository, productionLotRepository, productionEventRepository,
                serviceCategoryRepository, jobWorkerProfileRepository,
                fileStorageService, notificationService
        );

        buyer = new User("buyer@texsetu.demo", "hash", "Buyer Org", "+919879000000", Role.BUYER);
        buyer.setId(10L);

        otherBuyer = new User("other@texsetu.demo", "hash", "Other Org", "+919879000001", Role.BUYER);
        otherBuyer.setId(99L);

        category = new ServiceCategory("DYE_FABRIC_PROC", "Dyeing", "Processing");
        category.setId(1L);

        User u1 = new User("w1@texsetu.demo", "hash", "Worker 1", "+919825000001", Role.JOB_WORKER);
        u1.setId(21L);
        worker1 = new JobWorkerProfile();
        worker1.setId(1L);
        worker1.setUser(u1);
        worker1.setBusinessName("Mill Alpha");

        User u2 = new User("w2@texsetu.demo", "hash", "Worker 2", "+919825000002", Role.JOB_WORKER);
        u2.setId(22L);
        worker2 = new JobWorkerProfile();
        worker2.setId(2L);
        worker2.setUser(u2);
        worker2.setBusinessName("Mill Beta");
    }

    @Test
    @DisplayName("TC-06: Create RFQ with valid quantity and future completion date")
    void testCreateRfqSuccess() throws IOException {
        RfqCreateDto dto = new RfqCreateDto();
        dto.setCategoryId(1L);
        dto.setTitle("Continuous Dyeing 5000 Mts Cotton");
        dto.setFabricDescription("100% Cotton 40s Cambric");
        dto.setQuantity(BigDecimal.valueOf(5000));
        dto.setUnitOfMeasure(UnitOfMeasure.METERS);
        dto.setDesiredCompletionDate(LocalDate.now().plusDays(10));
        dto.setProcessingRequirements("Soft finish reactive black");

        when(serviceCategoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(rfqRepository.save(any(Rfq.class))).thenAnswer(i -> {
            Rfq r = i.getArgument(0);
            r.setId(501L);
            return r;
        });

        Rfq result = service.createRfq(buyer, dto);

        assertNotNull(result);
        assertEquals(RfqStatus.OPEN, result.getStatus());
        assertTrue(result.getRfqNumber().startsWith("RFQ-SRT-"));
        assertEquals(BigDecimal.valueOf(5000), result.getQuantity());
    }

    @Test
    @DisplayName("TC-07: RFQ creation rejected if quantity is zero or negative")
    void testCreateRfqInvalidQuantity() {
        RfqCreateDto dto = new RfqCreateDto();
        dto.setQuantity(BigDecimal.ZERO);
        dto.setDesiredCompletionDate(LocalDate.now().plusDays(5));

        assertThrows(IllegalArgumentException.class, () -> service.createRfq(buyer, dto));
    }

    @Test
    @DisplayName("TC-08: RFQ creation rejected if desired completion date is in the past")
    void testCreateRfqPastDate() {
        RfqCreateDto dto = new RfqCreateDto();
        dto.setQuantity(BigDecimal.valueOf(100));
        dto.setDesiredCompletionDate(LocalDate.now().minusDays(1));

        assertThrows(IllegalArgumentException.class, () -> service.createRfq(buyer, dto));
    }

    @Test
    @DisplayName("TC-09: Submitting a valid quotation updates RFQ status to QUOTED")
    void testSubmitQuotationUpdatesRfqStatus() {
        Rfq rfq = new Rfq();
        rfq.setId(100L);
        rfq.setRfqNumber("RFQ-100");
        rfq.setStatus(RfqStatus.OPEN);
        rfq.setBuyer(buyer);

        QuotationSubmitDto dto = new QuotationSubmitDto();
        dto.setRfqId(100L);
        dto.setUnitPrice(BigDecimal.valueOf(15.00));
        dto.setTotalPrice(BigDecimal.valueOf(75000.00));
        dto.setEstimatedDurationDays(4);
        dto.setProposedCompletionDate(LocalDate.now().plusDays(7));
        dto.setValidityDate(LocalDate.now().plusDays(5));

        when(rfqRepository.findById(100L)).thenReturn(Optional.of(rfq));
        when(quotationRepository.existsByRfqIdAndJobWorkerId(100L, worker1.getId())).thenReturn(false);
        when(quotationRepository.save(any(Quotation.class))).thenAnswer(i -> {
            Quotation q = i.getArgument(0);
            q.setId(201L);
            return q;
        });

        Quotation result = service.submitQuotation(worker1, dto);

        assertNotNull(result);
        assertEquals(QuotationStatus.SUBMITTED, result.getStatus());
        assertEquals(RfqStatus.QUOTED, rfq.getStatus());
        verify(rfqRepository, times(1)).save(rfq);
        verify(notificationService, times(1)).sendNotification(eq(buyer), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("TC-10: Prevent duplicate quotation submission by same provider on same RFQ")
    void testPreventDuplicateQuotation() {
        Rfq rfq = new Rfq();
        rfq.setId(100L);
        rfq.setStatus(RfqStatus.OPEN);

        QuotationSubmitDto dto = new QuotationSubmitDto();
        dto.setRfqId(100L);
        dto.setUnitPrice(BigDecimal.valueOf(10));
        dto.setTotalPrice(BigDecimal.valueOf(1000));

        when(rfqRepository.findById(100L)).thenReturn(Optional.of(rfq));
        when(quotationRepository.existsByRfqIdAndJobWorkerId(100L, worker1.getId())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.submitQuotation(worker1, dto));
        verify(quotationRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-11: Atomic Quotation Acceptance: Accept quote Q1 closes RFQ, rejects Q2, creates JobOrder & Lot Passport")
    void testAtomicQuotationAcceptance() {
        Rfq rfq = new Rfq();
        rfq.setId(100L);
        rfq.setRfqNumber("RFQ-100");
        rfq.setBuyer(buyer);
        rfq.setCategory(category);
        rfq.setQuantity(BigDecimal.valueOf(2000));
        rfq.setUnitOfMeasure(UnitOfMeasure.METERS);
        rfq.setStatus(RfqStatus.QUOTED);

        Quotation q1 = new Quotation();
        q1.setId(1L);
        q1.setQuoteNumber("QUO-1");
        q1.setRfq(rfq);
        q1.setJobWorker(worker1);
        q1.setTotalPrice(BigDecimal.valueOf(30000));
        q1.setProposedCompletionDate(LocalDate.now().plusDays(5));
        q1.setStatus(QuotationStatus.SUBMITTED);

        Quotation q2 = new Quotation();
        q2.setId(2L);
        q2.setQuoteNumber("QUO-2");
        q2.setRfq(rfq);
        q2.setJobWorker(worker2);
        q2.setTotalPrice(BigDecimal.valueOf(32000));
        q2.setStatus(QuotationStatus.SUBMITTED);

        when(quotationRepository.findById(1L)).thenReturn(Optional.of(q1));
        when(quotationRepository.findByRfqIdOrderByTotalPriceAsc(100L)).thenReturn(List.of(q1, q2));
        when(jobOrderRepository.save(any(JobOrder.class))).thenAnswer(i -> {
            JobOrder o = i.getArgument(0);
            o.setId(901L);
            return o;
        });
        when(productionLotRepository.save(any(ProductionLot.class))).thenAnswer(i -> {
            ProductionLot lot = i.getArgument(0);
            lot.setId(801L);
            return lot;
        });

        JobOrder order = service.acceptQuotation(1L, buyer);

        assertNotNull(order);
        assertEquals(RfqStatus.CLOSED, rfq.getStatus());
        assertEquals(QuotationStatus.ACCEPTED, q1.getStatus());
        assertEquals(QuotationStatus.REJECTED, q2.getStatus());
        assertEquals(OrderStatus.CONFIRMED, order.getCurrentStatus());

        verify(productionLotRepository, times(1)).save(any(ProductionLot.class));
        verify(productionEventRepository, times(1)).save(any(ProductionEvent.class));
    }

    @Test
    @DisplayName("TC-12: Concurrency check: Cannot accept quotation if RFQ is already CLOSED")
    void testAcceptQuotationOnClosedRfqThrows() {
        Rfq rfq = new Rfq();
        rfq.setId(100L);
        rfq.setBuyer(buyer);
        rfq.setStatus(RfqStatus.CLOSED);

        Quotation q1 = new Quotation();
        q1.setId(1L);
        q1.setRfq(rfq);
        q1.setStatus(QuotationStatus.SUBMITTED);

        when(quotationRepository.findById(1L)).thenReturn(Optional.of(q1));

        assertThrows(IllegalStateException.class, () -> service.acceptQuotation(1L, buyer));
        verify(jobOrderRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-13: Security check: Unauthorized buyer attempting to accept quote throws SecurityException")
    void testUnauthorizedBuyerAcceptanceThrows() {
        Rfq rfq = new Rfq();
        rfq.setId(100L);
        rfq.setBuyer(buyer);
        rfq.setStatus(RfqStatus.QUOTED);

        Quotation q1 = new Quotation();
        q1.setId(1L);
        q1.setRfq(rfq);

        when(quotationRepository.findById(1L)).thenReturn(Optional.of(q1));

        assertThrows(SecurityException.class, () -> service.acceptQuotation(1L, otherBuyer));
    }
}
