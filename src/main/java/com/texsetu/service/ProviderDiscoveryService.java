package com.texsetu.service;

import com.texsetu.entity.JobWorkerProfile;
import com.texsetu.entity.Review;
import com.texsetu.entity.enums.VerificationStatus;
import com.texsetu.repository.JobWorkerProfileRepository;
import com.texsetu.repository.ReviewRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        return jobWorkerProfileRepository.searchProviders(cleanCity, status, categoryId, cleanKeyword, pageable);
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
