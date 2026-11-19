package com.texsetu;

import com.texsetu.entity.*;
import com.texsetu.entity.enums.Role;
import com.texsetu.entity.enums.UnitOfMeasure;
import com.texsetu.repository.JobOrderRepository;
import com.texsetu.repository.ProductionEventRepository;
import com.texsetu.repository.ProductionLotRepository;
import com.texsetu.service.ProductionLotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductionLotPassportTest {

    @Mock private ProductionLotRepository productionLotRepository;
    @Mock private ProductionEventRepository productionEventRepository;
    @Mock private JobOrderRepository jobOrderRepository;

    private ProductionLotService service;

    private User workerUser;
    private User otherUser;
    private JobWorkerProfile worker;
    private JobOrder order;
    private ProductionLot lot;

    @BeforeEach
    void setUp() {
        service = new ProductionLotService(productionLotRepository, productionEventRepository, jobOrderRepository);

        workerUser = new User("worker@demo.com", "hash", "Worker", "+919825000000", Role.JOB_WORKER);
        workerUser.setId(20L);

        otherUser = new User("intruder@demo.com", "hash", "Intruder", "+919825000099", Role.JOB_WORKER);
        otherUser.setId(99L);

        worker = new JobWorkerProfile();
        worker.setId(1L);
        worker.setUser(workerUser);

        order = new JobOrder();
        order.setId(10L);
        order.setOrderNumber("ORD-10");
        order.setJobWorker(worker);
        order.setUnitOfMeasure(UnitOfMeasure.METERS);

        lot = new ProductionLot();
        lot.setId(100L);
        lot.setLotNumber("LOT-SRT-100");
        lot.setOrder(order);
        lot.setOriginalSubmittedQuantity(BigDecimal.valueOf(2500));
        lot.setReceivedQuantity(BigDecimal.valueOf(2500));
    }

    @Test
    @DisplayName("TC-27: Material reconciliation updates physical received quantity and appends audit event")
    void testReconcileMaterialSuccess() {
        when(productionLotRepository.findById(100L)).thenReturn(Optional.of(lot));
        when(productionLotRepository.save(any(ProductionLot.class))).thenAnswer(i -> i.getArgument(0));

        ProductionLot result = service.reconcileReceivedMaterial(
                100L, BigDecimal.valueOf(2480.50), "Received 5 rolls. 19.50 mts shrinkage on roll 2.", workerUser
        );

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(2480.50), result.getReceivedQuantity());
        assertEquals("MATERIAL_RECEIVED_VERIFIED", result.getCurrentStage());
        verify(productionEventRepository, times(1)).save(any(ProductionEvent.class));
    }

    @Test
    @DisplayName("TC-28: Unauthorized user cannot reconcile material for an unassigned order")
    void testUnauthorizedReconciliationThrows() {
        when(productionLotRepository.findById(100L)).thenReturn(Optional.of(lot));

        assertThrows(SecurityException.class, () -> service.reconcileReceivedMaterial(
                100L, BigDecimal.valueOf(2480), "Unauthorized attempt", otherUser
        ));
        verify(productionEventRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-29: Reconciling negative or zero quantity throws IllegalArgumentException")
    void testNegativeQuantityReconciliationThrows() {
        when(productionLotRepository.findById(100L)).thenReturn(Optional.of(lot));

        assertThrows(IllegalArgumentException.class, () -> service.reconcileReceivedMaterial(
                100L, BigDecimal.valueOf(-50), "Invalid negative qty", workerUser
        ));
    }
}
