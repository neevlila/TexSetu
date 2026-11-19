package com.texsetu.repository;

import com.texsetu.entity.PaymentRecord;
import com.texsetu.entity.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, Long> {
    Optional<PaymentRecord> findByTransactionReference(String transactionReference);
    List<PaymentRecord> findByOrderId(Long orderId);
    List<PaymentRecord> findByPayerId(Long payerId);
    List<PaymentRecord> findByStatus(PaymentStatus status);
}
