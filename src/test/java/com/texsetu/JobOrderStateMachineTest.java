package com.texsetu;

import com.texsetu.dto.ProductionUpdateDto;
import com.texsetu.entity.*;
import com.texsetu.entity.enums.OrderStatus;
import com.texsetu.entity.enums.Role;
import com.texsetu.notification.NotificationService;
import com.texsetu.repository.JobOrderRepository;
import com.texsetu.repository.ProductionEventRepository;
import com.texsetu.repository.ProductionLotRepository;
import com.texsetu.service.JobOrderService;
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
public class JobOrderStateMachineTest {

    @Mock private JobOrderRepository jobOrderRepository;
    @Mock private ProductionLotRepository productionLotRepository;
    @Mock private ProductionEventRepository productionEventRepository;
    @Mock private FileStorageService fileStorageService;
    @Mock private NotificationService notificationService;

    private JobOrderService service;

    private User buyer;
    private User workerUser;
    private JobWorkerProfile worker;
    private JobOrder order;
    private ProductionLot lot;

    @BeforeEach
    void setUp() {
        service = new JobOrderService(jobOrderRepository, productionLotRepository, productionEventRepository, fileStorageService, notificationService);

        buyer = new User("buyer@demo.com", "hash", "Buyer Name", "+919879000000", Role.BUYER);
        buyer.setId(10L);

        workerUser = new User("worker@demo.com", "hash", "Worker Name", "+919825000000", Role.JOB_WORKER);
        workerUser.setId(20L);

        worker = new JobWorkerProfile();
        worker.setId(2L);
        worker.setUser(workerUser);

        order = new JobOrder();
        order.setId(100L);
        order.setOrderNumber("ORD-100");
        order.setBuyer(buyer);
        order.setJobWorker(worker);
        order.setCurrentStatus(OrderStatus.CONFIRMED);

        lot = new ProductionLot();
        lot.setId(50L);
        lot.setOrder(order);
        lot.setOriginalSubmittedQuantity(BigDecimal.valueOf(1000));
        lot.setReceivedQuantity(BigDecimal.valueOf(1000));
    }

    @Test
    @DisplayName("TC-14 to TC-18: OrderStatus state machine validation rules")
    void testOrderStatusTransitionRules() {
        // Valid transitions
        assertTrue(OrderStatus.CONFIRMED.canTransitionTo(OrderStatus.MATERIAL_RECEIVED));
        assertTrue(OrderStatus.MATERIAL_RECEIVED.canTransitionTo(OrderStatus.PROCESSING));
        assertTrue(OrderStatus.PROCESSING.canTransitionTo(OrderStatus.QUALITY_CHECK));
        assertTrue(OrderStatus.QUALITY_CHECK.canTransitionTo(OrderStatus.COMPLETED));
        assertTrue(OrderStatus.QUALITY_CHECK.canTransitionTo(OrderStatus.PROCESSING)); // Rework
        assertTrue(OrderStatus.COMPLETED.canTransitionTo(OrderStatus.DISPATCHED));
        assertTrue(OrderStatus.DISPATCHED.canTransitionTo(OrderStatus.DELIVERED));

        // Invalid transitions
        assertFalse(OrderStatus.CONFIRMED.canTransitionTo(OrderStatus.COMPLETED));
        assertFalse(OrderStatus.CONFIRMED.canTransitionTo(OrderStatus.DISPATCHED));
        assertFalse(OrderStatus.DELIVERED.canTransitionTo(OrderStatus.PROCESSING));
        assertFalse(OrderStatus.CANCELLED.canTransitionTo(OrderStatus.CONFIRMED));
    }

    @Test
    @DisplayName("TC-19: Illegal status skip (CONFIRMED to COMPLETED) throws IllegalArgumentException")
    void testIllegalSkipThrows() {
        ProductionUpdateDto dto = new ProductionUpdateDto();
        dto.setOrderId(100L);
        dto.setNextStatus(OrderStatus.COMPLETED); // Illegal skip!
        dto.setStageName("DIRECT_COMPLETION");
        dto.setDescription("Attempting invalid jump");

        when(jobOrderRepository.findById(100L)).thenReturn(Optional.of(order));

        assertThrows(IllegalArgumentException.class, () -> service.updateOrderStatus(workerUser, dto));
        verify(jobOrderRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-20: Job Worker progressing CONFIRMED -> MATERIAL_RECEIVED logs ProductionEvent")
    void testWorkerAdvancesToMaterialReceived() throws IOException {
        ProductionUpdateDto dto = new ProductionUpdateDto();
        dto.setOrderId(100L);
        dto.setNextStatus(OrderStatus.MATERIAL_RECEIVED);
        dto.setStageName("RAW_MATERIAL_RECEIVED");
        dto.setDescription("Received 5 rolls of grey fabric at factory gate.");

        when(jobOrderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(productionLotRepository.findByOrderId(100L)).thenReturn(Optional.of(lot));
        when(jobOrderRepository.save(any(JobOrder.class))).thenReturn(order);

        JobOrder updated = service.updateOrderStatus(workerUser, dto);

        assertEquals(OrderStatus.MATERIAL_RECEIVED, updated.getCurrentStatus());
        verify(productionEventRepository, times(1)).save(any(ProductionEvent.class));
        verify(notificationService, times(1)).sendNotification(eq(buyer), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("TC-21: Worker role cannot mark order as DELIVERED (restricted to Buyer)")
    void testWorkerCannotMarkDelivered() {
        order.setCurrentStatus(OrderStatus.DISPATCHED);

        ProductionUpdateDto dto = new ProductionUpdateDto();
        dto.setOrderId(100L);
        dto.setNextStatus(OrderStatus.DELIVERED);
        dto.setStageName("DELIVERED");
        dto.setDescription("Worker attempting to self-mark delivery");

        when(jobOrderRepository.findById(100L)).thenReturn(Optional.of(order));

        assertThrows(SecurityException.class, () -> service.updateOrderStatus(workerUser, dto));
    }

    @Test
    @DisplayName("TC-22: Buyer confirming DELIVERED when status is DISPATCHED succeeds")
    void testBuyerConfirmsDelivered() throws IOException {
        order.setCurrentStatus(OrderStatus.DISPATCHED);

        ProductionUpdateDto dto = new ProductionUpdateDto();
        dto.setOrderId(100L);
        dto.setNextStatus(OrderStatus.DELIVERED);
        dto.setStageName("FINAL_DELIVERY_CONFIRMED");
        dto.setDescription("Received goods in warehouse.");

        when(jobOrderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(productionLotRepository.findByOrderId(100L)).thenReturn(Optional.of(lot));
        when(jobOrderRepository.save(any(JobOrder.class))).thenReturn(order);

        JobOrder updated = service.updateOrderStatus(buyer, dto);

        assertEquals(OrderStatus.DELIVERED, updated.getCurrentStatus());
        assertEquals("CLOSED", lot.getStatus());
        verify(productionEventRepository, times(1)).save(any(ProductionEvent.class));
    }
}
