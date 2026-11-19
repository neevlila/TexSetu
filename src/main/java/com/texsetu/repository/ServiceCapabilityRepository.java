package com.texsetu.repository;

import com.texsetu.entity.ServiceCapability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceCapabilityRepository extends JpaRepository<ServiceCapability, Long> {
    List<ServiceCapability> findByJobWorkerId(Long jobWorkerId);
    List<ServiceCapability> findByJobWorkerIdAndActiveTrue(Long jobWorkerId);
    List<ServiceCapability> findByCategoryIdAndActiveTrue(Long categoryId);
}
