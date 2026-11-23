package com.texsetu.service;

import com.texsetu.entity.JobWorkerProfile;
import com.texsetu.entity.Review;
import com.texsetu.entity.ServiceCapability;
import com.texsetu.entity.ServiceCategory;
import com.texsetu.entity.enums.VerificationStatus;
import com.texsetu.repository.JobWorkerProfileRepository;
import com.texsetu.repository.ReviewRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProviderDiscoveryService {

    private final JobWorkerProfileRepository jobWorkerProfileRepository;
    private final ReviewRepository reviewRepository;

    public ProviderDiscoveryService(JobWorkerProfileRepository jobWorkerProfileRepository,
                                    ReviewRepository reviewRepository) {
        this.jobWorkerProfileRepository = jobWorkerProfileRepository;
        this.reviewRepository = reviewRepository;
    }

    @Transactional(readOnly = true)
    public Page<JobWorkerProfile> discoverProviders(String city, VerificationStatus status, Long categoryId, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("businessName").ascending());
        String cleanCity = (city != null && !city.isBlank()) ? city.trim() : null;
        String cleanKeyword = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;

        Specification<JobWorkerProfile> spec = (root, query, cb) -> {
            query.distinct(true);
            List<Predicate> predicates = new ArrayList<>();

            if (cleanCity != null) {
                predicates.add(cb.like(cb.lower(root.get("city")), "%" + cleanCity.toLowerCase() + "%"));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("verificationStatus"), status));
            }

            if (categoryId != null) {
                Join<JobWorkerProfile, ServiceCapability> capJoin = root.join("capabilities", JoinType.INNER);
                Join<ServiceCapability, ServiceCategory> catJoin = capJoin.join("category", JoinType.INNER);
                predicates.add(cb.equal(catJoin.get("id"), categoryId));
            }

            if (cleanKeyword != null) {
                String term = "%" + cleanKeyword.toLowerCase() + "%";
                Join<JobWorkerProfile, ServiceCapability> capJoin = root.join("capabilities", JoinType.LEFT);
                Predicate p1 = cb.like(cb.lower(root.get("businessName")), term);
                Predicate p2 = cb.and(
                        cb.isNotNull(root.get("description")),
                        cb.like(cb.lower(root.get("description")), term)
                );
                Predicate p3 = cb.and(
                        cb.isNotNull(capJoin.get("serviceName")),
                        cb.like(cb.lower(capJoin.get("serviceName")), term)
                );
                predicates.add(cb.or(p1, p2, p3));
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };

        return jobWorkerProfileRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<JobWorkerProfile> getProviderDetails(Long id) {
        return jobWorkerProfileRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Review> getProviderReviews(Long workerUserId) {
        return reviewRepository.findByRevieweeIdOrderByCreatedAtDesc(workerUserId);
    }

    @Transactional(readOnly = true)
    public double getAverageRating(Long workerUserId) {
        List<Review> reviews = reviewRepository.findByRevieweeIdOrderByCreatedAtDesc(workerUserId);
        if (reviews.isEmpty()) return 0.0;
        return reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
    }
}
