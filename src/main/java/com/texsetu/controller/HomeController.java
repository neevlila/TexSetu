package com.texsetu.controller;

import com.texsetu.entity.enums.VerificationStatus;
import com.texsetu.repository.JobOrderRepository;
import com.texsetu.repository.JobWorkerProfileRepository;
import com.texsetu.repository.RfqRepository;
import com.texsetu.service.CatalogService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final CatalogService catalogService;
    private final JobWorkerProfileRepository jobWorkerProfileRepository;
    private final RfqRepository rfqRepository;
    private final JobOrderRepository jobOrderRepository;

    public HomeController(CatalogService catalogService,
                          JobWorkerProfileRepository jobWorkerProfileRepository,
                          RfqRepository rfqRepository,
                          JobOrderRepository jobOrderRepository) {
        this.catalogService = catalogService;
        this.jobWorkerProfileRepository = jobWorkerProfileRepository;
        this.rfqRepository = rfqRepository;
        this.jobOrderRepository = jobOrderRepository;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("categories", catalogService.getAllActiveCategories());
        model.addAttribute("featuredWorkers", jobWorkerProfileRepository.findByVerificationStatus(VerificationStatus.VERIFIED));
        model.addAttribute("verifiedProviderCount", jobWorkerProfileRepository.countByVerificationStatus(VerificationStatus.VERIFIED));
        model.addAttribute("totalRfqCount", rfqRepository.count());
        model.addAttribute("totalOrderCount", jobOrderRepository.count());
        return "public/index";
    }

    @GetMapping("/about")
    public String about() {
        return "public/about";
    }

    @GetMapping("/how-it-works")
    public String howItWorks() {
        return "public/how-it-works";
    }

    @GetMapping("/privacy")
    public String privacy() {
        return "public/privacy";
    }

    @GetMapping("/terms")
    public String terms() {
        return "public/terms";
    }

    @GetMapping("/error/403")
    public String accessDenied() {
        return "error/403";
    }
}
