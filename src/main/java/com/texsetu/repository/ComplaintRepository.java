package com.texsetu.repository;

import com.texsetu.entity.Complaint;
import com.texsetu.entity.enums.ComplaintStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    Optional<Complaint> findByComplaintReference(String reference);
    List<Complaint> findByOrderId(Long orderId);
    List<Complaint> findByComplainantIdOrderByCreatedAtDesc(Long complainantId);
    List<Complaint> findByStatus(ComplaintStatus status);
    long countByStatus(ComplaintStatus status);
}
