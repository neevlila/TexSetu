package com.texsetu.service;

import com.texsetu.dto.ReviewCreateDto;
import com.texsetu.entity.JobOrder;
import com.texsetu.entity.Review;
import com.texsetu.entity.User;
import com.texsetu.entity.enums.OrderStatus;
import com.texsetu.repository.JobOrderRepository;
import com.texsetu.repository.ReviewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    private static final Logger log = LoggerFactory.getLogger(ReviewService.class);

    private final ReviewRepository reviewRepository;
    private final JobOrderRepository jobOrderRepository;

    public ReviewService(ReviewRepository reviewRepository, JobOrderRepository jobOrderRepository) {
        this.reviewRepository = reviewRepository;
        this.jobOrderRepository = jobOrderRepository;
    }

    @Transactional
    public Review submitReview(User reviewer, ReviewCreateDto dto) {
        JobOrder order = jobOrderRepository.findById(dto.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Job order not found with ID: " + dto.getOrderId()));

        // Check if order is in eligible completed or delivered state
        if (order.getCurrentStatus() != OrderStatus.COMPLETED && order.getCurrentStatus() != OrderStatus.DELIVERED) {
            throw new IllegalStateException("Reviews can only be submitted for completed or delivered orders.");
        }

        // Only the buyer who ordered can submit a review
        if (!order.getBuyer().getId().equals(reviewer.getId())) {
            throw new SecurityException("Unauthorized: Only the buyer of this order can submit a review.");
        }

        // Check for duplicate review
        if (reviewRepository.existsByOrderId(order.getId())) {
            throw new IllegalStateException("A review has already been submitted for this order.");
        }

        Review review = new Review();
        review.setOrder(order);
        review.setReviewer(reviewer);
        review.setReviewee(order.getJobWorker().getUser());
        review.setRating(dto.getRating());
        review.setComment(dto.getComment().trim());

        Review savedReview = reviewRepository.save(review);
        log.info("Review submitted for Order {} with rating {}", order.getOrderNumber(), dto.getRating());
        return savedReview;
    }

    @Transactional(readOnly = true)
    public Optional<Review> getReviewForOrder(Long orderId) {
        return reviewRepository.findByOrderId(orderId);
    }

    @Transactional(readOnly = true)
    public List<Review> getReviewsForWorker(Long workerUserId) {
        return reviewRepository.findByRevieweeIdOrderByCreatedAtDesc(workerUserId);
    }
}
