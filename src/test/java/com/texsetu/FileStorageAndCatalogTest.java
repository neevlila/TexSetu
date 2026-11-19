package com.texsetu;

import com.texsetu.entity.JobWorkerProfile;
import com.texsetu.entity.ServiceCapability;
import com.texsetu.entity.ServiceCategory;
import com.texsetu.entity.enums.UnitOfMeasure;
import com.texsetu.repository.JobWorkerProfileRepository;
import com.texsetu.repository.ServiceCapabilityRepository;
import com.texsetu.repository.ServiceCategoryRepository;
import com.texsetu.service.CatalogService;
import com.texsetu.storage.LocalFileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FileStorageAndCatalogTest {

    private LocalFileStorageService storageService;

    @Mock private ServiceCategoryRepository serviceCategoryRepository;
    @Mock private ServiceCapabilityRepository serviceCapabilityRepository;
    @Mock private JobWorkerProfileRepository jobWorkerProfileRepository;

    private CatalogService catalogService;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        storageService = new LocalFileStorageService(tempDir.toString());
        catalogService = new CatalogService(serviceCategoryRepository, serviceCapabilityRepository, jobWorkerProfileRepository);
    }

    @Test
    @DisplayName("TC-36: File storage validation rejects empty file")
    void testValidateEmptyFileThrows() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[0]);
        assertThrows(IllegalArgumentException.class, () -> storageService.validateFile(emptyFile));
    }

    @Test
    @DisplayName("TC-37: File storage validation rejects files exceeding 10MB")
    void testValidateLargeFileThrows() {
        byte[] largeBytes = new byte[11 * 1024 * 1024]; // 11MB
        MockMultipartFile largeFile = new MockMultipartFile("file", "large.pdf", "application/pdf", largeBytes);
        assertThrows(IllegalArgumentException.class, () -> storageService.validateFile(largeFile));
    }

    @Test
    @DisplayName("TC-38: File storage rejects prohibited executable MIME types")
    void testValidateProhibitedMimeTypeThrows() {
        MockMultipartFile exeFile = new MockMultipartFile("file", "malicious.exe", "application/x-msdownload", "evil".getBytes());
        assertThrows(IllegalArgumentException.class, () -> storageService.validateFile(exeFile));
    }

    @Test
    @DisplayName("TC-39: Valid image file stores successfully and returns safe URL")
    void testStoreValidImageFile() throws IOException {
        MockMultipartFile validImage = new MockMultipartFile("file", "swatch.png", "image/png", "sample-image-data".getBytes());
        String url = storageService.storeFile(validImage, "swatches");

        assertNotNull(url);
        assertTrue(url.startsWith("/uploads/swatches/"));
        assertTrue(url.endsWith(".png"));
    }

    @Test
    @DisplayName("TC-40: CatalogService addCapability validates positive MOQ and positive price")
    void testAddCapabilityValidation() {
        JobWorkerProfile worker = new JobWorkerProfile();
        worker.setId(1L);
        ServiceCategory cat = new ServiceCategory("DYE", "Dyeing", "Processing");
        cat.setId(2L);

        when(jobWorkerProfileRepository.findById(1L)).thenReturn(Optional.of(worker));
        when(serviceCategoryRepository.findById(2L)).thenReturn(Optional.of(cat));
        when(serviceCapabilityRepository.save(any(ServiceCapability.class))).thenAnswer(i -> i.getArgument(0));

        ServiceCapability cap = catalogService.addCapability(
                1L, 2L, "Soft Flow Dyeing", "Rayon",
                BigDecimal.valueOf(1000), UnitOfMeasure.METERS, BigDecimal.valueOf(15.50), 4, "Scouring included"
        );

        assertNotNull(cap);
        assertEquals("Soft Flow Dyeing", cap.getServiceName());
        assertEquals(BigDecimal.valueOf(15.50), cap.getIndicativePricePerUnit());

        // Negative MOQ should throw
        assertThrows(IllegalArgumentException.class, () -> catalogService.addCapability(
                1L, 2L, "Bad MOQ", "Desc", BigDecimal.valueOf(-10), UnitOfMeasure.METERS, BigDecimal.valueOf(15), 3, "Notes"
        ));
    }
}
