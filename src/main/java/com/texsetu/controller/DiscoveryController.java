package com.texsetu.controller;

import com.texsetu.entity.JobWorkerProfile;
import com.texsetu.entity.Review;
import com.texsetu.entity.enums.VerificationStatus;
import com.texsetu.service.CatalogService;
import com.texsetu.service.ProviderDiscoveryService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class DiscoveryController {

    private final ProviderDiscoveryService providerDiscoveryService;
    private final CatalogService catalogService;

    public DiscoveryController(ProviderDiscoveryService providerDiscoveryService,
                               CatalogService catalogService) {
        this.providerDiscoveryService = providerDiscoveryService;
        this.catalogService = catalogService;
    }

    @GetMapping("/discovery")
    public String browseProviders(@RequestParam(required = false) String city,
                                  @RequestParam(required = false) Long categoryId,
                                  @RequestParam(required = false) String keyword,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size,
                                  Model model) {
        Page<JobWorkerProfile> providers = providerDiscoveryService.discoverProviders(
                city, null, categoryId, keyword, page, size
        );

        model.addAttribute("providers", providers);
        model.addAttribute("categories", catalogService.getAllActiveCategories());
        model.addAttribute("selectedCity", city);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", providers.getTotalPages());
        return "public/discovery";
    }

    @GetMapping("/providers/{id}")
    public String viewProviderDetails(@PathVariable Long id, Model model) {
        JobWorkerProfile provider = providerDiscoveryService.getProviderDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("Provider not found with ID: " + id));

        List<Review> reviews = providerDiscoveryService.getProviderReviews(provider.getUser().getId());
        double avgRating = providerDiscoveryService.getAverageRating(provider.getUser().getId());

        model.addAttribute("provider", provider);
        model.addAttribute("capabilities", catalogService.getActiveCapabilitiesForWorker(provider.getId()));
        model.addAttribute("reviews", reviews);
        model.addAttribute("avgRating", avgRating);
        return "public/provider-details";
    }
}
