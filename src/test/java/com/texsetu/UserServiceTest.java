package com.texsetu;

import com.texsetu.dto.UserRegistrationDto;
import com.texsetu.entity.User;
import com.texsetu.entity.enums.AccountStatus;
import com.texsetu.entity.enums.Role;
import com.texsetu.repository.BusinessProfileRepository;
import com.texsetu.repository.JobWorkerProfileRepository;
import com.texsetu.repository.UserRepository;
import com.texsetu.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private BusinessProfileRepository businessProfileRepository;
    @Mock
    private JobWorkerProfileRepository jobWorkerProfileRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, businessProfileRepository, jobWorkerProfileRepository, passwordEncoder);
    }

    @Test
    @DisplayName("TC-01: Successfully register a new buyer with business profile")
    void testRegisterBuyerSuccess() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setFullName("Arun Mehta");
        dto.setEmail("arun.mehta@surattextile.demo");
        dto.setPhoneNumber("+91 98250 12345");
        dto.setPassword("SecretPass123!");
        dto.setRole(Role.BUYER);
        dto.setBusinessName("Mehta Silk Mills");
        dto.setBusinessType("TRADER");
        dto.setAddress("Ring Road Market");
        dto.setCity("Surat");
        dto.setState("Gujarat");
        dto.setPincode("395002");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("SecretPass123!")).thenReturn("$2a$10$encodedPasswordHash");
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(101L);
            return u;
        });

        User result = userService.registerUser(dto);

        assertNotNull(result);
        assertEquals("arun.mehta@surattextile.demo", result.getEmail());
        assertEquals(Role.BUYER, result.getRole());
        assertEquals(AccountStatus.ACTIVE, result.getAccountStatus());
        verify(businessProfileRepository, times(1)).save(any());
        verify(jobWorkerProfileRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-02: Registering with an existing email throws IllegalArgumentException")
    void testRegisterDuplicateEmail() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setEmail("existing@texsetu.demo");
        when(userRepository.existsByEmail("existing@texsetu.demo")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(dto));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-03: Registration as ADMIN role is strictly prohibited")
    void testRegisterAdminBlocked() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setEmail("fakeadmin@texsetu.demo");
        dto.setRole(Role.ADMIN);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(dto));
    }

    @Test
    @DisplayName("TC-04: Successfully register a Job Worker and initialize worker profile")
    void testRegisterJobWorkerSuccess() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setFullName("Kishorebhai Gariwala");
        dto.setEmail("kishore.embroidery@texsetu.demo");
        dto.setPhoneNumber("+91 98251 99999");
        dto.setPassword("WorkerPass123!");
        dto.setRole(Role.JOB_WORKER);
        dto.setBusinessName("Kishore Laser Embroidery");
        dto.setBusinessType("EMBROIDERY_UNIT");
        dto.setAddress("Sachin GIDC Road 3");
        dto.setCity("Surat");
        dto.setState("Gujarat");
        dto.setPincode("394230");
        dto.setIndicativeCapacity(3000.0);
        dto.setTurnaroundDays(4);

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encodedHash");
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(102L);
            return u;
        });

        User result = userService.registerUser(dto);

        assertNotNull(result);
        assertEquals(Role.JOB_WORKER, result.getRole());
        verify(jobWorkerProfileRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("TC-05: Password change validates current password before updating")
    void testChangePasswordValidation() {
        User user = new User("user@test.com", "$2a$10$currentHash", "User Test", "+919825000000", Role.BUYER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPass", "$2a$10$currentHash")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> userService.changePassword(1L, "WrongPass", "NewPass123!"));
        verify(userRepository, never()).save(user);
    }
}
