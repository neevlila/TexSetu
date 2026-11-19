package com.texsetu.payment;

import com.texsetu.entity.JobOrder;
import com.texsetu.entity.PaymentRecord;
import com.texsetu.entity.User;
import com.texsetu.entity.enums.PaymentStatus;
import com.texsetu.repository.PaymentRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class MockPaymentGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(MockPaymentGateway.class);
    private final PaymentRecordRepository paymentRecordRepository;

    public MockPaymentGateway(PaymentRecordRepository paymentRecordRepository) {
        this.paymentRecordRepository = paymentRecordRepository;
    }

    @Override
    @Transactional
    public PaymentRecord initiateSimulatedPayment(JobOrder order, User payer, BigDecimal amount) {
        String txRef = "TXN-SIM-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        PaymentRecord record = new PaymentRecord();
        record.setTransactionReference(txRef);
        record.setOrder(order);
        record.setPayer(payer);
        record.setPayee(order.getJobWorker().getUser());
        record.setAmount(amount);
        record.setCurrency("INR");
        record.setPaymentMethod("SIMULATED_B2B_ESCROW");
        record.setStatus(PaymentStatus.INITIATED);
        record.setGatewayResponse("{\"status\":\"INITIATED\",\"mock_gateway\":\"TexSetu Sandbox\",\"auth_code\":\"TX-" + System.currentTimeMillis() + "\"}");

        PaymentRecord saved = paymentRecordRepository.save(record);
        log.info("Simulated payment initiated: Ref={} for Order={}", txRef, order.getOrderNumber());
        return saved;
    }

    @Override
    @Transactional
    public PaymentRecord completeSimulatedPayment(String transactionReference, boolean shouldSucceed) {
        PaymentRecord record = paymentRecordRepository.findByTransactionReference(transactionReference)
                .orElseThrow(() -> new IllegalArgumentException("Transaction reference not found: " + transactionReference));

        if (shouldSucceed) {
            record.setStatus(PaymentStatus.SUCCESS);
            record.setGatewayResponse("{\"status\":\"SUCCESS\",\"auth_code\":\"MOCK-AUTH-200\",\"message\":\"Funds placed in simulated milestone escrow.\"}");
            log.info("Simulated payment succeeded: {}", transactionReference);
        } else {
            record.setStatus(PaymentStatus.FAILED);
            record.setGatewayResponse("{\"status\":\"FAILED\",\"error_code\":\"MOCK-ERR-402\",\"message\":\"Simulated payment declined by bank simulator.\"}");
            log.warn("Simulated payment failed: {}", transactionReference);
        }

        return paymentRecordRepository.save(record);
    }

    @Override
    @Transactional
    public PaymentRecord processSimulatedRefund(String transactionReference, String refundReason) {
        PaymentRecord record = paymentRecordRepository.findByTransactionReference(transactionReference)
                .orElseThrow(() -> new IllegalArgumentException("Transaction reference not found: " + transactionReference));

        record.setStatus(PaymentStatus.REFUNDED);
        record.setGatewayResponse("{\"status\":\"REFUNDED\",\"reason\":\"" + refundReason + "\",\"refund_id\":\"REF-" + UUID.randomUUID().toString().substring(0, 8) + "\"}");
        log.info("Simulated refund processed for: {}", transactionReference);

        return paymentRecordRepository.save(record);
    }
}
