package com.texsetu.repository;

import com.texsetu.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByOrderId(Long orderId);
    List<Review> findByRevieweeIdOrderByCreatedAtDesc(Long revieweeId);
    boolean existsByOrderId(Long orderId);
}
