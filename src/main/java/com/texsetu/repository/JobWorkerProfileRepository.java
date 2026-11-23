package com.texsetu.repository;

import com.texsetu.entity.JobWorkerProfile;
import com.texsetu.entity.User;
import com.texsetu.entity.enums.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobWorkerProfileRepository extends JpaRepository<JobWorkerProfile, Long>, JpaSpecificationExecutor<JobWorkerProfile> {
    Optional<JobWorkerProfile> findByUser(User user);
    Optional<JobWorkerProfile> findByUserId(Long userId);
    List<JobWorkerProfile> findByVerificationStatus(VerificationStatus status);
    long countByVerificationStatus(VerificationStatus status);

    @Query(value = "SELECT DISTINCT j FROM JobWorkerProfile j " +
           "LEFT JOIN j.capabilities c " +
           "LEFT JOIN c.category cat " +
           "WHERE (:city IS NULL OR LOWER(j.city) LIKE LOWER(CONCAT('%', :city, '%'))) " +
           "AND (:status IS NULL OR j.verificationStatus = :status) " +
           "AND (:categoryId IS NULL OR cat.id = :categoryId) " +
           "AND (:keyword IS NULL OR (LOWER(j.businessName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR (j.description IS NOT NULL AND LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "     OR (c.serviceName IS NOT NULL AND LOWER(c.serviceName) LIKE LOWER(CONCAT('%', :keyword, '%')))))",
           countQuery = "SELECT COUNT(DISTINCT j) FROM JobWorkerProfile j " +
           "LEFT JOIN j.capabilities c " +
           "LEFT JOIN c.category cat " +
           "WHERE (:city IS NULL OR LOWER(j.city) LIKE LOWER(CONCAT('%', :city, '%'))) " +
           "AND (:status IS NULL OR j.verificationStatus = :status) " +
           "AND (:categoryId IS NULL OR cat.id = :categoryId) " +
           "AND (:keyword IS NULL OR (LOWER(j.businessName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR (j.description IS NOT NULL AND LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "     OR (c.serviceName IS NOT NULL AND LOWER(c.serviceName) LIKE LOWER(CONCAT('%', :keyword, '%')))))")
    Page<JobWorkerProfile> searchProviders(@Param("city") String city,
                                          @Param("status") VerificationStatus status,
                                          @Param("categoryId") Long categoryId,
                                          @Param("keyword") String keyword,
                                          Pageable pageable);
}
