package com.texsetu.repository;

import com.texsetu.entity.QualityInspection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QualityInspectionRepository extends JpaRepository<QualityInspection, Long> {
    List<QualityInspection> findByProductionLotIdOrderByInspectionDateDesc(Long lotId);
    List<QualityInspection> findByOrderIdOrderByInspectionDateDesc(Long orderId);
}
