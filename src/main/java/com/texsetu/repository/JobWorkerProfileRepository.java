package com.texsetu.repository;

import com.texsetu.entity.JobWorkerProfile;
import com.texsetu.entity.User;
import com.texsetu.entity.enums.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobWorkerProfileRepository extends JpaRepository<JobWorkerProfile, Long> {
    Optional<JobWorkerProfile> findByUser(User user);
    Optional<JobWorkerProfile> findByUserId(Long userId);
    List<JobWorkerProfile> findByVerificationStatus(VerificationStatus status);
    long countByVerificationStatus(VerificationStatus status);

    @Query("SELECT DISTINCT j FROM JobWorkerProfile j " +
           "LEFT JOIN j.capabilities c " +
           "WHERE (:city IS NULL OR LOWER(j.city) LIKE LOWER(CONCAT('%', :city, '%'))) " +
           "AND (:status IS NULL OR j.verificationStatus = :status) " +
           "AND (:categoryId IS NULL OR c.category.id = :categoryId) " +
           "AND (:keyword IS NULL OR LOWER(j.businessName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(c.serviceName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<JobWorkerProfile> searchProviders(@Param("city") String city,
                                          @Param("status") VerificationStatus status,
                                          @Param("categoryId") Long categoryId,
                                          @Param("keyword") String keyword,
                                          Pageable pageable);
}
