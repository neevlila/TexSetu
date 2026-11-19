package com.texsetu.config;

import com.texsetu.entity.*;
import com.texsetu.entity.enums.AccountStatus;
import com.texsetu.entity.enums.Role;
import com.texsetu.entity.enums.UnitOfMeasure;
import com.texsetu.entity.enums.VerificationStatus;
import com.texsetu.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@Profile("dev")
public class InitialDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(InitialDataSeeder.class);

    private final UserRepository userRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final JobWorkerProfileRepository jobWorkerProfileRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;
    private final ServiceCapabilityRepository serviceCapabilityRepository;
    private final PasswordEncoder passwordEncoder;

    public InitialDataSeeder(UserRepository userRepository,
                             BusinessProfileRepository businessProfileRepository,
                             JobWorkerProfileRepository jobWorkerProfileRepository,
                             ServiceCategoryRepository serviceCategoryRepository,
                             ServiceCapabilityRepository serviceCapabilityRepository,
                             PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.businessProfileRepository = businessProfileRepository;
        this.jobWorkerProfileRepository = jobWorkerProfileRepository;
        this.serviceCategoryRepository = serviceCategoryRepository;
        this.serviceCapabilityRepository = serviceCapabilityRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already seeded with {} users. Skipping initial seed.", userRepository.count());
            return;
        }

        log.info("Seeding initial TexSetu development data for Surat textile market...");

        // 1. Seed Categories
        ServiceCategory catDyeing = new ServiceCategory("DYE_FABRIC_PROC", "Dyeing and Fabric Processing",
                "Continuous dyeing, soft flow jet dyeing, mercerization, bleaching, and chemical finishing.");
        ServiceCategory catPrint = new ServiceCategory("PRINT_EMBROIDERY", "Printing and Embroidery",
                "High-speed rotary screen printing, reactive digital textile printing, and multi-head computerized embroidery.");
        ServiceCategory catStitch = new ServiceCategory("STITCH_FINISHING", "Garment Stitching and Finishing",
                "Industrial sewing, flatlock/overlock stitching, button-holing, industrial steam pressing, and export packing.");

        catDyeing = serviceCategoryRepository.save(catDyeing);
        catPrint = serviceCategoryRepository.save(catPrint);
        catStitch = serviceCategoryRepository.save(catStitch);

        String defaultPass = passwordEncoder.encode("Password123!");

        // 2. Seed Admin
        User admin = new User("admin@texsetu.com", defaultPass, "TexSetu Master Admin", "+91 98250 11000", Role.ADMIN);
        userRepository.save(admin);

        // 3. Seed Job-Worker 1: Radhe Krishna Dyeing (Verified)
        User worker1 = new User("radhe.dyeing@texsetu.demo", defaultPass, "Rameshchandra Patel", "+91 98251 22334", Role.JOB_WORKER);
        worker1 = userRepository.save(worker1);

        BusinessProfile bp1 = new BusinessProfile();
        bp1.setUser(worker1);
        bp1.setBusinessName("Radhe Krishna Dyeing & Processing Mills");
        bp1.setBusinessType("JOB_WORK_UNIT");
        bp1.setGstNumber("24AAACR1234F1Z1");
        bp1.setPanNumber("AAACR1234F");
        bp1.setAddress("Plot No. 42-45, Phase 2, Pandesara GIDC");
        bp1.setCity("Surat");
        bp1.setState("Gujarat");
        bp1.setPincode("394221");
        bp1.setContactPerson("Ramesh Patel");
        bp1.setContactPhone("+91 98251 22334");
        bp1.setContactEmail("radhe.dyeing@texsetu.demo");
        businessProfileRepository.save(bp1);

        JobWorkerProfile jwp1 = new JobWorkerProfile();
        jwp1.setUser(worker1);
        jwp1.setBusinessName("Radhe Krishna Dyeing & Processing Mills");
        jwp1.setBusinessType("Dyeing & Processing Mill");
        jwp1.setDescription("Leading cotton, modal, and rayon dyeing unit with multi-ton soft flow jet dyeing and stenter machines.");
        jwp1.setCity("Surat");
        jwp1.setServiceArea("Pandesara & South Gujarat");
        jwp1.setAddress("Plot 42-45, Phase 2, Pandesara GIDC");
        jwp1.setMachineryDetails("4x Soft Flow Jet Dyeing (Sclavos), 2x 8-Chamber Stenters (Monforts), Continuous Bleaching Range");
        jwp1.setIndicativeCapacityMetersPerDay(BigDecimal.valueOf(25000.0));
        jwp1.setTurnaroundDays(4);
        jwp1.setVerificationStatus(VerificationStatus.VERIFIED);
        jwp1.setVerificationNotes("Physical GIDC premises and pollution control certificates verified.");
        jwp1.setVerifiedAt(LocalDateTime.now());
        jwp1 = jobWorkerProfileRepository.save(jwp1);

        ServiceCapability cap1 = new ServiceCapability();
        cap1.setJobWorker(jwp1);
        cap1.setCategory(catDyeing);
        cap1.setServiceName("Reactive Jet Dyeing (Rayon / Viscose)");
        cap1.setDescription("Precision shade matching with high colorfastness reactive dyes on high-grade viscose and rayon fabrics.");
        cap1.setMinimumQuantity(BigDecimal.valueOf(1000.0));
        cap1.setUnitOfMeasure(UnitOfMeasure.METERS);
        cap1.setIndicativePricePerUnit(BigDecimal.valueOf(14.50));
        cap1.setLeadTimeDays(4);
        cap1.setProcessingDetails("Includes desizing, scouring, bleaching, dyeing, and stenter heat-setting.");
        cap1.setActive(true);
        serviceCapabilityRepository.save(cap1);

        // 4. Seed Job-Worker 2: Surat Digital Prints (Verified)
        User worker2 = new User("surat.digital@texsetu.demo", defaultPass, "Ketanbhai Shah", "+91 98252 33445", Role.JOB_WORKER);
        worker2 = userRepository.save(worker2);

        BusinessProfile bp2 = new BusinessProfile();
        bp2.setUser(worker2);
        bp2.setBusinessName("Surat Digital Prints & Laser Embroidery");
        bp2.setBusinessType("JOB_WORK_UNIT");
        bp2.setGstNumber("24AABCS5678G1Z2");
        bp2.setPanNumber("AABCS5678G");
        bp2.setAddress("Shed 12B, Road No. 6, Sachin GIDC");
        bp2.setCity("Surat");
        bp2.setState("Gujarat");
        bp2.setPincode("394230");
        bp2.setContactPerson("Ketan Shah");
        bp2.setContactPhone("+91 98252 33445");
        bp2.setContactEmail("surat.digital@texsetu.demo");
        businessProfileRepository.save(bp2);

        JobWorkerProfile jwp2 = new JobWorkerProfile();
        jwp2.setUser(worker2);
        jwp2.setBusinessName("Surat Digital Prints & Laser Embroidery");
        jwp2.setBusinessType("Digital Printing & Embroidery Hub");
        jwp2.setDescription("Direct-to-fabric reactive digital printing and computerized multi-head Tajima embroidery.");
        jwp2.setCity("Surat");
        jwp2.setServiceArea("Sachin GIDC & Surat City");
        jwp2.setAddress("Shed 12B, Road No. 6, Sachin GIDC");
        jwp2.setMachineryDetails("3x Kyocera High-Speed Digital Printers (1200 dpi), 4x 32-Head Tajima Computerized Embroidery Machines");
        jwp2.setIndicativeCapacityMetersPerDay(BigDecimal.valueOf(8000.0));
        jwp2.setTurnaroundDays(3);
        jwp2.setVerificationStatus(VerificationStatus.VERIFIED);
        jwp2.setVerificationNotes("Registered GST verified, machinery ownership verified.");
        jwp2.setVerifiedAt(LocalDateTime.now());
        jwp2 = jobWorkerProfileRepository.save(jwp2);

        ServiceCapability cap2 = new ServiceCapability();
        cap2.setJobWorker(jwp2);
        cap2.setCategory(catPrint);
        cap2.setServiceName("High-Definition Reactive Digital Printing");
        cap2.setDescription("Direct reactive digital printing on pure cotton, silk, and viscose up to 64-inch width.");
        cap2.setMinimumQuantity(BigDecimal.valueOf(500.0));
        cap2.setUnitOfMeasure(UnitOfMeasure.METERS);
        cap2.setIndicativePricePerUnit(BigDecimal.valueOf(48.00));
        cap2.setLeadTimeDays(3);
        cap2.setProcessingDetails("Sub-millimeter color alignment, steaming, washing, and loop drying included.");
        cap2.setActive(true);
        serviceCapabilityRepository.save(cap2);

        // 5. Seed Job-Worker 3: Shree Ambica (Pending Verification)
        User worker3 = new User("ambica.unverified@texsetu.demo", defaultPass, "Pravinbhai Solanki", "+91 98254 55667", Role.JOB_WORKER);
        worker3 = userRepository.save(worker3);

        BusinessProfile bp3 = new BusinessProfile();
        bp3.setUser(worker3);
        bp3.setBusinessName("Shree Ambica Textile Jobworks");
        bp3.setBusinessType("JOB_WORK_UNIT");
        bp3.setAddress("Survey 88, Near Bhestan Railway Crossing, Udhna");
        bp3.setCity("Surat");
        bp3.setState("Gujarat");
        bp3.setPincode("394210");
        bp3.setContactPerson("Pravin Solanki");
        bp3.setContactPhone("+91 98254 55667");
        bp3.setContactEmail("ambica.unverified@texsetu.demo");
        businessProfileRepository.save(bp3);

        JobWorkerProfile jwp3 = new JobWorkerProfile();
        jwp3.setUser(worker3);
        jwp3.setBusinessName("Shree Ambica Textile Jobworks");
        jwp3.setBusinessType("Small Scale Embroidery Job-Work");
        jwp3.setDescription("Independent job-work unit catering to local boutique and wholesale saree embroidery orders.");
        jwp3.setCity("Surat");
        jwp3.setServiceArea("Udhna & Bhestan");
        jwp3.setAddress("Survey 88, Near Bhestan Railway Crossing, Udhna");
        jwp3.setMachineryDetails("2x 12-Head Multi-needle Embroidery Machines");
        jwp3.setIndicativeCapacityMetersPerDay(BigDecimal.valueOf(800.0));
        jwp3.setTurnaroundDays(6);
        jwp3.setVerificationStatus(VerificationStatus.PENDING);
        jwp3.setVerificationNotes("Awaiting municipal trade license submission.");
        jobWorkerProfileRepository.save(jwp3);

        // 6. Seed Buyer 1: Meera Fashion House
        User buyer1 = new User("meera.fashions@texsetu.demo", defaultPass, "Meera Singhania", "+91 98790 66778", Role.BUYER);
        buyer1 = userRepository.save(buyer1);

        BusinessProfile bpBuyer1 = new BusinessProfile();
        bpBuyer1.setUser(buyer1);
        bpBuyer1.setBusinessName("Meera Fashion House");
        bpBuyer1.setBusinessType("CLOTHING_BRAND");
        bpBuyer1.setGstNumber("24AAFCM7890K1Z5");
        bpBuyer1.setPanNumber("AAFCM7890K");
        bpBuyer1.setAddress("Shop 412, Surat Textile Market (STM), Ring Road");
        bpBuyer1.setCity("Surat");
        bpBuyer1.setState("Gujarat");
        bpBuyer1.setPincode("395002");
        bpBuyer1.setContactPerson("Meera Singhania");
        bpBuyer1.setContactPhone("+91 98790 66778");
        bpBuyer1.setContactEmail("meera.fashions@texsetu.demo");
        businessProfileRepository.save(bpBuyer1);

        log.info("TexSetu initial data successfully seeded.");
    }
}
