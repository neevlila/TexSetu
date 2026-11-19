package com.texsetu.repository;

import com.texsetu.entity.VerificationRecord;
import com.texsetu.entity.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VerificationRecordRepository extends JpaRepository<VerificationRecord, Long> {
    List<VerificationRecord> findByJobWorkerIdOrderByCreatedAtDesc(Long jobWorkerId);
    List<VerificationRecord> findByStatusOrderByCreatedAtDesc(VerificationStatus status);
}
