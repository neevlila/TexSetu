package com.texsetu.service;

import com.texsetu.dto.UserRegistrationDto;
import com.texsetu.entity.BusinessProfile;
import com.texsetu.entity.JobWorkerProfile;
import com.texsetu.entity.User;
import com.texsetu.entity.enums.AccountStatus;
import com.texsetu.entity.enums.Role;
import com.texsetu.entity.enums.VerificationStatus;
import com.texsetu.repository.BusinessProfileRepository;
import com.texsetu.repository.JobWorkerProfileRepository;
import com.texsetu.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final JobWorkerProfileRepository jobWorkerProfileRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       BusinessProfileRepository businessProfileRepository,
                       JobWorkerProfileRepository jobWorkerProfileRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.businessProfileRepository = businessProfileRepository;
        this.jobWorkerProfileRepository = jobWorkerProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerUser(UserRegistrationDto dto) {
        if (userRepository.existsByEmail(dto.getEmail().toLowerCase().trim())) {
            throw new IllegalArgumentException("An account with email " + dto.getEmail() + " already exists.");
        }

        // Prevent registration with ADMIN role
        if (dto.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException("Cannot register as an administrator.");
        }

        User user = new User();
        user.setEmail(dto.getEmail().toLowerCase().trim());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setFullName(dto.getFullName().trim());
        user.setPhoneNumber(dto.getPhoneNumber().trim());
        user.setRole(dto.getRole());
        user.setAccountStatus(AccountStatus.ACTIVE);

        User savedUser = userRepository.save(user);

        // Create BusinessProfile
        BusinessProfile bp = new BusinessProfile();
        bp.setUser(savedUser);
        bp.setBusinessName(dto.getBusinessName().trim());
        bp.setBusinessType(dto.getBusinessType().trim());
        bp.setGstNumber(dto.getGstNumber() != null ? dto.getGstNumber().trim() : null);
        bp.setPanNumber(dto.getPanNumber() != null ? dto.getPanNumber().trim() : null);
        bp.setAddress(dto.getAddress().trim());
        bp.setCity(dto.getCity().trim());
        bp.setState(dto.getState().trim());
        bp.setPincode(dto.getPincode().trim());
        bp.setContactPerson(dto.getFullName().trim());
        bp.setContactPhone(dto.getPhoneNumber().trim());
        bp.setContactEmail(dto.getEmail().toLowerCase().trim());
        businessProfileRepository.save(bp);

        // If JOB_WORKER, initialize JobWorkerProfile with PENDING verification
        if (dto.getRole() == Role.JOB_WORKER) {
            JobWorkerProfile jwp = new JobWorkerProfile();
            jwp.setUser(savedUser);
            jwp.setBusinessName(dto.getBusinessName().trim());
            jwp.setBusinessType(dto.getBusinessType().trim());
            jwp.setAddress(dto.getAddress().trim());
            jwp.setCity(dto.getCity().trim());
            jwp.setServiceArea(dto.getCity().trim() + " & South Gujarat");
            jwp.setMachineryDetails(dto.getMachineryDetails());
            jwp.setIndicativeCapacityMetersPerDay(dto.getIndicativeCapacity() != null ? BigDecimal.valueOf(dto.getIndicativeCapacity()) : BigDecimal.valueOf(1000.0));
            jwp.setTurnaroundDays(dto.getTurnaroundDays() != null ? dto.getTurnaroundDays() : 3);
            jwp.setVerificationStatus(VerificationStatus.PENDING);
            jwp.setVerificationNotes("Registered online. Pending administrator review.");
            jwp.setCapacityLastUpdated(LocalDateTime.now());
            jobWorkerProfileRepository.save(jwp);
        }

        log.info("Registered new user {} with role {}", savedUser.getEmail(), savedUser.getRole());
        return savedUser;
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase().trim());
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password does not match.");
        }
        if (newPassword.length() < 6) {
            throw new IllegalArgumentException("New password must be at least 6 characters.");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password successfully changed for user {}", user.getEmail());
    }

    @Transactional
    public void updateAccountStatus(Long userId, AccountStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setAccountStatus(status);
        userRepository.save(user);
        log.info("Updated account status for user {} to {}", user.getEmail(), status);
    }
}
