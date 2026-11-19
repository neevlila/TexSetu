package com.texsetu;

import com.texsetu.dto.InspectionCreateDto;
import com.texsetu.entity.*;
import com.texsetu.entity.enums.InspectionResult;
import com.texsetu.entity.enums.OrderStatus;
import com.texsetu.entity.enums.Role;
import com.texsetu.entity.enums.UnitOfMeasure;
import com.texsetu.notification.NotificationService;
import com.texsetu.repository.JobOrderRepository;
import com.texsetu.repository.ProductionEventRepository;
import com.texsetu.repository.ProductionLotRepository;
import com.texsetu.repository.QualityInspectionRepository;
import com.texsetu.service.QualityInspectionService;
import com.texsetu.storage.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class QualityInspectionAndReworkTest {

    @Mock private QualityInspectionRepository qualityInspectionRepository;
    @Mock private JobOrderRepository jobOrderRepository;
    @Mock private ProductionLotRepository productionLotRepository;
    @Mock private ProductionEventRepository productionEventRepository;
    @Mock private FileStorageService fileStorageService;
    @Mock private NotificationService notificationService;

    private QualityInspectionService service;

    private User buyer;
    private User workerUser;
    private JobWorkerProfile worker;
    private JobOrder order;
    private ProductionLot lot;

    @BeforeEach
    void setUp() {
        service = new QualityInspectionService(
                qualityInspectionRepository, jobOrderRepository,
                productionLotRepository, productionEventRepository,
                fileStorageService, notificationService
        );

        buyer = new User("buyer@demo.com", "hash", "Buyer Org", "+919879000000", Role.BUYER);
        buyer.setId(10L);

        workerUser = new User("worker@demo.com", "hash", "Worker Org", "+919825000000", Role.JOB_WORKER);
        workerUser.setId(20L);

        worker = new JobWorkerProfile();
        worker.setId(2L);
        worker.setUser(workerUser);

        order = new JobOrder();
        order.setId(100L);
        order.setOrderNumber("ORD-100");
        order.setBuyer(buyer);
        order.setJobWorker(worker);
        order.setUnitOfMeasure(UnitOfMeasure.METERS);
        order.setCurrentStatus(OrderStatus.QUALITY_CHECK);

        lot = new ProductionLot();
        lot.setId(50L);
        lot.setOrder(order);
        lot.setOriginalSubmittedQuantity(BigDecimal.valueOf(1000));
        lot.setReceivedQuantity(BigDecimal.valueOf(1000));
        lot.setCurrentStage("READY_FOR_INSPECTION");
    }

    @Test
    @DisplayName("TC-23: Quality inspection ACCEPTED marks order COMPLETED and closes production lot")
    void testQualityInspectionAccepted() throws IOException {
        InspectionCreateDto dto = new InspectionCreateDto();
        dto.setOrderId(100L);
        dto.setQuantityInspected(BigDecimal.valueOf(1000));
        dto.setAcceptedQuantity(BigDecimal.valueOf(990));
        dto.setRejectedQuantity(BigDecimal.valueOf(10));
        dto.setResult(InspectionResult.ACCEPTED);
        dto.setInspectionComments("Shade matching delta E < 0.5. Tensile strength passed. Lot approved.");

        when(jobOrderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(productionLotRepository.findByOrderId(100L)).thenReturn(Optional.of(lot));
        when(qualityInspectionRepository.save(any(QualityInspection.class))).thenAnswer(i -> i.getArgument(0));

        QualityInspection result = service.recordInspection(buyer, dto);

        assertNotNull(result);
        assertEquals(InspectionResult.ACCEPTED, result.getResult());
        assertEquals(OrderStatus.COMPLETED, order.getCurrentStatus());
        assertEquals("CLOSED", lot.getStatus());
        assertEquals(BigDecimal.valueOf(990), lot.getQuantityCompleted());

        verify(productionEventRepository, times(1)).save(any(ProductionEvent.class));
        verify(notificationService, times(1)).sendNotification(eq(workerUser), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("TC-24: Quality inspection REWORK_REQUIRED moves order back to PROCESSING and flags rework")
    void testQualityInspectionReworkRequired() throws IOException {
        InspectionCreateDto dto = new InspectionCreateDto();
        dto.setOrderId(100L);
        dto.setQuantityInspected(BigDecimal.valueOf(1000));
        dto.setAcceptedQuantity(BigDecimal.valueOf(700));
        dto.setRejectedQuantity(BigDecimal.valueOf(300));
        dto.setResult(InspectionResult.REWORK_REQUIRED);
        dto.setInspectionComments("Uneven shade on roll #3 and #4. Requires re-stenter and soft wash.");
        dto.setDefectDetails("Edge shade banding defect");

        when(jobOrderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(productionLotRepository.findByOrderId(100L)).thenReturn(Optional.of(lot));
        when(qualityInspectionRepository.save(any(QualityInspection.class))).thenAnswer(i -> i.getArgument(0));

        QualityInspection result = service.recordInspection(buyer, dto);

        assertNotNull(result);
        assertEquals(InspectionResult.REWORK_REQUIRED, result.getResult());
        assertEquals(OrderStatus.PROCESSING, order.getCurrentStatus()); // Reverted to PROCESSING for rework!
        assertEquals("REWORK", lot.getStatus());
        assertEquals("REWORK_IN_PROGRESS", lot.getCurrentStage());

        verify(productionEventRepository, times(1)).save(any(ProductionEvent.class));
        verify(notificationService, times(1)).sendNotification(eq(workerUser), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("TC-25: Inspected quantity math validation (accepted + rejected > total inspected throws)")
    void testQuantityMathValidationThrows() {
        InspectionCreateDto dto = new InspectionCreateDto();
        dto.setOrderId(100L);
        dto.setQuantityInspected(BigDecimal.valueOf(100));
        dto.setAcceptedQuantity(BigDecimal.valueOf(80));
        dto.setRejectedQuantity(BigDecimal.valueOf(30)); // 80 + 30 = 110 > 100!
        dto.setResult(InspectionResult.REWORK_REQUIRED);
        dto.setInspectionComments("Invalid tally test");

        when(jobOrderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(productionLotRepository.findByOrderId(100L)).thenReturn(Optional.of(lot));

        assertThrows(IllegalArgumentException.class, () -> service.recordInspection(buyer, dto));
        verify(qualityInspectionRepository, never()).save(any());
    }
}
