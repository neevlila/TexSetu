package com.texsetu.repository;

import com.texsetu.entity.BusinessProfile;
import com.texsetu.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BusinessProfileRepository extends JpaRepository<BusinessProfile, Long> {
    Optional<BusinessProfile> findByUser(User user);
    Optional<BusinessProfile> findByUserId(Long userId);
}
