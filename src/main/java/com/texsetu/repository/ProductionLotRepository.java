package com.texsetu.repository;

import com.texsetu.entity.ProductionLot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductionLotRepository extends JpaRepository<ProductionLot, Long> {
    Optional<ProductionLot> findByLotNumber(String lotNumber);
    Optional<ProductionLot> findByOrderId(Long orderId);
}
