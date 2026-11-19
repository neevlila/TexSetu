package com.texsetu;

import com.texsetu.entity.*;
import com.texsetu.entity.enums.OrderStatus;
import com.texsetu.entity.enums.Role;
import com.texsetu.entity.enums.UnitOfMeasure;
import com.texsetu.entity.enums.VerificationStatus;
import com.texsetu.repository.JobOrderRepository;
import com.texsetu.repository.JobWorkerProfileRepository;
import com.texsetu.repository.ProductionLotRepository;
import com.texsetu.service.CsvReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CsvReportServiceTest {

    @Mock private JobOrderRepository jobOrderRepository;
    @Mock private JobWorkerProfileRepository jobWorkerProfileRepository;
    @Mock private ProductionLotRepository productionLotRepository;

    private CsvReportService service;

    @BeforeEach
    void setUp() {
        service = new CsvReportService(jobOrderRepository, jobWorkerProfileRepository, productionLotRepository);
    }

    @Test
    @DisplayName("TC-34: CSV export of job orders includes proper headers and row values")
    void testExportJobOrdersToCsv() throws IOException {
        User buyer = new User("buyer@demo.com", "hash", "Buyer Org", "+919879000000", Role.BUYER);
        User workerUser = new User("worker@demo.com", "hash", "Worker Org", "+919825000000", Role.JOB_WORKER);
        JobWorkerProfile worker = new JobWorkerProfile();
        worker.setBusinessName("Shree Ram Dyeing");
        worker.setUser(workerUser);

        ServiceCategory cat = new ServiceCategory("DYE", "Dyeing", "Processing");
        Rfq rfq = new Rfq();
        rfq.setRfqNumber("RFQ-TEST-1");

        JobOrder order = new JobOrder();
        order.setOrderNumber("ORD-SRT-999");
        order.setRfq(rfq);
        order.setBuyer(buyer);
        order.setJobWorker(worker);
        order.setCategory(cat);
        order.setAgreedQuantity(BigDecimal.valueOf(5000));
        order.setUnitOfMeasure(UnitOfMeasure.METERS);
        order.setAgreedPrice(BigDecimal.valueOf(72500.00));
        order.setCurrentStatus(OrderStatus.PROCESSING);
        order.setExpectedCompletionDate(LocalDate.now().plusDays(5));
        order.setCreatedAt(LocalDateTime.now());

        when(jobOrderRepository.findAll()).thenReturn(List.of(order));

        ByteArrayInputStream stream = service.exportJobOrdersToCsv();
        String csvContent = new String(stream.readAllBytes(), StandardCharsets.UTF_8);

        assertNotNull(csvContent);
        assertTrue(csvContent.contains("Order Number"));
        assertTrue(csvContent.contains("ORD-SRT-999"));
        assertTrue(csvContent.contains("Shree Ram Dyeing"));
        assertTrue(csvContent.contains("72500"));
    }

    @Test
    @DisplayName("TC-35: CSV export of providers includes provider capacity and verification status")
    void testExportProvidersToCsv() throws IOException {
        User workerUser = new User("worker@demo.com", "hash", "Ketan Shah", "+919825200000", Role.JOB_WORKER);
        JobWorkerProfile p = new JobWorkerProfile();
        p.setId(10L);
        p.setUser(workerUser);
        p.setBusinessName("Surat Digital Prints");
        p.setBusinessType("Digital Printing");
        p.setCity("Surat");
        p.setServiceArea("Sachin GIDC");
        p.setIndicativeCapacityMetersPerDay(BigDecimal.valueOf(8000));
        p.setTurnaroundDays(3);
        p.setVerificationStatus(VerificationStatus.VERIFIED);

        when(jobWorkerProfileRepository.findAll()).thenReturn(List.of(p));

        ByteArrayInputStream stream = service.exportProvidersToCsv();
        String csvContent = new String(stream.readAllBytes(), StandardCharsets.UTF_8);

        assertNotNull(csvContent);
        assertTrue(csvContent.contains("Provider ID"));
        assertTrue(csvContent.contains("Surat Digital Prints"));
        assertTrue(csvContent.contains("8000"));
        assertTrue(csvContent.contains("VERIFIED"));
    }
}
