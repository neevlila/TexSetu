package com.texsetu.repository;

import com.texsetu.entity.Quotation;
import com.texsetu.entity.enums.QuotationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuotationRepository extends JpaRepository<Quotation, Long> {
    Optional<Quotation> findByQuoteNumber(String quoteNumber);
    List<Quotation> findByRfqIdOrderByTotalPriceAsc(Long rfqId);
    List<Quotation> findByJobWorkerIdOrderByCreatedAtDesc(Long jobWorkerId);
    List<Quotation> findByJobWorkerIdAndStatus(Long jobWorkerId, QuotationStatus status);
    boolean existsByRfqIdAndJobWorkerId(Long rfqId, Long jobWorkerId);
    long countByJobWorkerId(Long jobWorkerId);
    long countByJobWorkerIdAndStatus(Long jobWorkerId, QuotationStatus status);
}
