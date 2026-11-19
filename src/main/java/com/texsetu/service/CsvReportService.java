package com.texsetu.service;

import com.texsetu.entity.JobOrder;
import com.texsetu.entity.JobWorkerProfile;
import com.texsetu.entity.ProductionLot;
import com.texsetu.repository.JobOrderRepository;
import com.texsetu.repository.JobWorkerProfileRepository;
import com.texsetu.repository.ProductionLotRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class CsvReportService {

    private static final Logger log = LoggerFactory.getLogger(CsvReportService.class);

    private final JobOrderRepository jobOrderRepository;
    private final JobWorkerProfileRepository jobWorkerProfileRepository;
    private final ProductionLotRepository productionLotRepository;

    public CsvReportService(JobOrderRepository jobOrderRepository,
                            JobWorkerProfileRepository jobWorkerProfileRepository,
                            ProductionLotRepository productionLotRepository) {
        this.jobOrderRepository = jobOrderRepository;
        this.jobWorkerProfileRepository = jobWorkerProfileRepository;
        this.productionLotRepository = productionLotRepository;
    }

    @Transactional(readOnly = true)
    public ByteArrayInputStream exportJobOrdersToCsv() throws IOException {
        List<JobOrder> orders = jobOrderRepository.findAll();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader("Order Number", "RFQ Number", "Buyer Email", "Job Worker Unit", "Category", "Agreed Quantity", "UOM", "Agreed Price (INR)", "Status", "Expected Date", "Created Date")
                .build();

        try (CSVPrinter printer = new CSVPrinter(new OutputStreamWriter(out, StandardCharsets.UTF_8), format)) {
            for (JobOrder order : orders) {
                printer.printRecord(
                        order.getOrderNumber(),
                        order.getRfq().getRfqNumber(),
                        order.getBuyer().getEmail(),
                        order.getJobWorker().getBusinessName(),
                        order.getCategory().getName(),
                        order.getAgreedQuantity(),
                        order.getUnitOfMeasure(),
                        order.getAgreedPrice(),
                        order.getCurrentStatus(),
                        order.getExpectedCompletionDate(),
                        order.getCreatedAt()
                );
            }
            printer.flush();
        }

        log.info("Exported {} job orders to CSV", orders.size());
        return new ByteArrayInputStream(out.toByteArray());
    }

    @Transactional(readOnly = true)
    public ByteArrayInputStream exportProvidersToCsv() throws IOException {
        List<JobWorkerProfile> providers = jobWorkerProfileRepository.findAll();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader("Provider ID", "Business Name", "Business Type", "City", "Service Area", "Capacity (M/Day)", "Turnaround (Days)", "Verification Status", "Contact Email")
                .build();

        try (CSVPrinter printer = new CSVPrinter(new OutputStreamWriter(out, StandardCharsets.UTF_8), format)) {
            for (JobWorkerProfile p : providers) {
                printer.printRecord(
                        p.getId(),
                        p.getBusinessName(),
                        p.getBusinessType(),
                        p.getCity(),
                        p.getServiceArea(),
                        p.getIndicativeCapacityMetersPerDay(),
                        p.getTurnaroundDays(),
                        p.getVerificationStatus(),
                        p.getUser().getEmail()
                );
            }
            printer.flush();
        }

        log.info("Exported {} providers to CSV", providers.size());
        return new ByteArrayInputStream(out.toByteArray());
    }

    @Transactional(readOnly = true)
    public ByteArrayInputStream exportProductionLotsToCsv() throws IOException {
        List<ProductionLot> lots = productionLotRepository.findAll();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader("Lot Number", "Order Number", "Category", "Original Submitted Qty", "Received Qty", "Current Stage", "Completed Qty", "Rejected/Waste Qty", "Status", "Created At")
                .build();

        try (CSVPrinter printer = new CSVPrinter(new OutputStreamWriter(out, StandardCharsets.UTF_8), format)) {
            for (ProductionLot lot : lots) {
                printer.printRecord(
                        lot.getLotNumber(),
                        lot.getOrder().getOrderNumber(),
                        lot.getProcessingCategory(),
                        lot.getOriginalSubmittedQuantity(),
                        lot.getReceivedQuantity(),
                        lot.getCurrentStage(),
                        lot.getQuantityCompleted(),
                        lot.getQuantityRejectedWaste(),
                        lot.getStatus(),
                        lot.getCreatedAt()
                );
            }
            printer.flush();
        }

        log.info("Exported {} production lots to CSV", lots.size());
        return new ByteArrayInputStream(out.toByteArray());
    }
}
