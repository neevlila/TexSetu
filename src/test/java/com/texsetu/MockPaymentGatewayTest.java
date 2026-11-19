package com.texsetu;

import com.texsetu.entity.*;
import com.texsetu.entity.enums.PaymentStatus;
import com.texsetu.entity.enums.Role;
import com.texsetu.payment.MockPaymentGateway;
import com.texsetu.repository.PaymentRecordRepository;
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
public class MockPaymentGatewayTest {

    @Mock private PaymentRecordRepository paymentRecordRepository;

    private MockPaymentGateway gateway;

    private User buyer;
    private User workerUser;
    private JobWorkerProfile worker;
    private JobOrder order;

    @BeforeEach
    void setUp() {
        gateway = new MockPaymentGateway(paymentRecordRepository);

        buyer = new User("buyer@demo.com", "hash", "Buyer Name", "+919879000000", Role.BUYER);
        buyer.setId(1L);

        workerUser = new User("worker@demo.com", "hash", "Worker Name", "+919825000000", Role.JOB_WORKER);
        workerUser.setId(2L);

        worker = new JobWorkerProfile();
        worker.setId(10L);
        worker.setUser(workerUser);

        order = new JobOrder();
        order.setId(100L);
        order.setOrderNumber("ORD-100");
        order.setBuyer(buyer);
        order.setJobWorker(worker);
    }

    @Test
    @DisplayName("TC-30: Simulated payment initiation generates INITIATED record and transaction ref")
    void testInitiatePayment() {
        when(paymentRecordRepository.save(any(PaymentRecord.class))).thenAnswer(i -> {
            PaymentRecord r = i.getArgument(0);
            r.setId(501L);
            return r;
        });

        PaymentRecord record = gateway.initiateSimulatedPayment(order, buyer, BigDecimal.valueOf(45000.00));

        assertNotNull(record);
        assertTrue(record.getTransactionReference().startsWith("TXN-SIM-"));
        assertEquals(PaymentStatus.INITIATED, record.getStatus());
        assertEquals(BigDecimal.valueOf(45000.00), record.getAmount());
    }

    @Test
    @DisplayName("TC-31: Completing payment with success sets status to SUCCESS")
    void testCompletePaymentSuccess() {
        PaymentRecord record = new PaymentRecord();
        record.setTransactionReference("TXN-SIM-123");
        record.setStatus(PaymentStatus.INITIATED);

        when(paymentRecordRepository.findByTransactionReference("TXN-SIM-123")).thenReturn(Optional.of(record));
        when(paymentRecordRepository.save(any(PaymentRecord.class))).thenAnswer(i -> i.getArgument(0));

        PaymentRecord completed = gateway.completeSimulatedPayment("TXN-SIM-123", true);

        assertEquals(PaymentStatus.SUCCESS, completed.getStatus());
        assertTrue(completed.getGatewayResponse().contains("SUCCESS"));
    }

    @Test
    @DisplayName("TC-32: Completing payment with failure sets status to FAILED")
    void testCompletePaymentFailure() {
        PaymentRecord record = new PaymentRecord();
        record.setTransactionReference("TXN-SIM-FAIL-1");
        record.setStatus(PaymentStatus.INITIATED);

        when(paymentRecordRepository.findByTransactionReference("TXN-SIM-FAIL-1")).thenReturn(Optional.of(record));
        when(paymentRecordRepository.save(any(PaymentRecord.class))).thenAnswer(i -> i.getArgument(0));

        PaymentRecord failed = gateway.completeSimulatedPayment("TXN-SIM-FAIL-1", false);

        assertEquals(PaymentStatus.FAILED, failed.getStatus());
        assertTrue(failed.getGatewayResponse().contains("FAILED"));
    }

    @Test
    @DisplayName("TC-33: Refund simulation transitions status to REFUNDED with reason")
    void testProcessRefund() {
        PaymentRecord record = new PaymentRecord();
        record.setTransactionReference("TXN-SIM-REF-1");
        record.setStatus(PaymentStatus.SUCCESS);

        when(paymentRecordRepository.findByTransactionReference("TXN-SIM-REF-1")).thenReturn(Optional.of(record));
        when(paymentRecordRepository.save(any(PaymentRecord.class))).thenAnswer(i -> i.getArgument(0));

        PaymentRecord refunded = gateway.processSimulatedRefund("TXN-SIM-REF-1", "Excess yardage shortage confirmed");

        assertEquals(PaymentStatus.REFUNDED, refunded.getStatus());
        assertTrue(refunded.getGatewayResponse().contains("REFUNDED"));
    }
}
