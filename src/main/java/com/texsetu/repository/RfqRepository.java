package com.texsetu.repository;

import com.texsetu.entity.Rfq;
import com.texsetu.entity.enums.RfqStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RfqRepository extends JpaRepository<Rfq, Long> {
    Optional<Rfq> findByRfqNumber(String rfqNumber);
    List<Rfq> findByBuyerIdOrderByCreatedAtDesc(Long buyerId);
    Page<Rfq> findByBuyerId(Long buyerId, Pageable pageable);
    List<Rfq> findByStatusOrderByCreatedAtDesc(RfqStatus status);
    List<Rfq> findByCategoryIdAndStatusOrderByCreatedAtDesc(Long categoryId, RfqStatus status);
    long countByStatus(RfqStatus status);
}
