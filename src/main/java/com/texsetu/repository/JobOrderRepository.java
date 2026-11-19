package com.texsetu.repository;

import com.texsetu.entity.JobOrder;
import com.texsetu.entity.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobOrderRepository extends JpaRepository<JobOrder, Long> {
    Optional<JobOrder> findByOrderNumber(String orderNumber);
    Optional<JobOrder> findByQuotationId(Long quotationId);
    List<JobOrder> findByBuyerIdOrderByCreatedAtDesc(Long buyerId);
    Page<JobOrder> findByBuyerId(Long buyerId, Pageable pageable);
    List<JobOrder> findByJobWorkerIdOrderByCreatedAtDesc(Long jobWorkerId);
    Page<JobOrder> findByJobWorkerId(Long jobWorkerId, Pageable pageable);
    List<JobOrder> findByCurrentStatus(OrderStatus status);
    long countByBuyerId(Long buyerId);
    long countByJobWorkerId(Long jobWorkerId);
    long countByJobWorkerIdAndCurrentStatus(Long jobWorkerId, OrderStatus status);
    long countByCurrentStatus(OrderStatus status);
}
