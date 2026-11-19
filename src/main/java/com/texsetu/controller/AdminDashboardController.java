package com.texsetu.controller;

import com.texsetu.entity.JobWorkerProfile;
import com.texsetu.entity.ServiceCategory;
import com.texsetu.entity.User;
import com.texsetu.entity.enums.AccountStatus;
import com.texsetu.entity.enums.ComplaintStatus;
import com.texsetu.entity.enums.VerificationStatus;
import com.texsetu.repository.JobWorkerProfileRepository;
import com.texsetu.repository.UserRepository;
import com.texsetu.service.AdminService;
import com.texsetu.service.CatalogService;
import com.texsetu.service.ComplaintService;
import com.texsetu.service.CsvReportService;
import com.texsetu.service.UserService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    private final AdminService adminService;
    private final UserService userService;
    private final CatalogService catalogService;
    private final ComplaintService complaintService;
    private final CsvReportService csvReportService;
    private final UserRepository userRepository;
    private final JobWorkerProfileRepository jobWorkerProfileRepository;

    public AdminDashboardController(AdminService adminService,
                                  UserService userService,
                                  CatalogService catalogService,
                                  ComplaintService complaintService,
                                  CsvReportService csvReportService,
                                  UserRepository userRepository,
                                  JobWorkerProfileRepository jobWorkerProfileRepository) {
        this.adminService = adminService;
        this.userService = userService;
        this.catalogService = catalogService;
        this.complaintService = complaintService;
        this.csvReportService = csvReportService;
        this.userRepository = userRepository;
        this.jobWorkerProfileRepository = jobWorkerProfileRepository;
    }

    private User getAuthenticatedAdmin(UserDetails userDetails) {
        return userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Admin not found."));
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        var stats = adminService.getAdminDashboardStats();
        List<JobWorkerProfile> pending = adminService.getPendingVerifications();
        model.addAttribute("stats", stats);
        model.addAttribute("pendingVerifications", pending);
        return "admin/dashboard";
    }

    @GetMapping("/verifications")
    public String verifications(Model model) {
        model.addAttribute("pendingList", adminService.getPendingVerifications());
        model.addAttribute("allProviders", jobWorkerProfileRepository.findAll());
        return "admin/verifications";
    }

    @PostMapping("/verifications/{id}/process")
    public String processVerification(@PathVariable Long id,
                                      @AuthenticationPrincipal UserDetails userDetails,
                                      @RequestParam VerificationStatus status,
                                      @RequestParam(required = false) String reviewNotes,
                                      RedirectAttributes redirectAttributes) {
        User admin = getAuthenticatedAdmin(userDetails);
        try {
            adminService.processProviderVerification(id, admin, status, reviewNotes);
            redirectAttributes.addFlashAttribute("successMessage", "Provider status updated to " + status);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/verifications";
    }

    @GetMapping("/users")
    public String manageUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "admin/users";
    }

    @PostMapping("/users/{id}/toggle-status")
    public String toggleUserStatus(@PathVariable Long id,
                                   @RequestParam AccountStatus status,
                                   RedirectAttributes redirectAttributes) {
        try {
            adminService.toggleUserAccountStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage", "User account status updated to " + status);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/categories")
    public String categories(Model model) {
        model.addAttribute("categories", catalogService.getAllActiveCategories());
        return "admin/categories";
    }

    @PostMapping("/categories/save")
    public String saveCategory(@RequestParam(required = false) Long id,
                               @RequestParam String code,
                               @RequestParam String name,
                               @RequestParam(required = false) String description,
                               @RequestParam(defaultValue = "true") Boolean active,
                               RedirectAttributes redirectAttributes) {
        try {
            adminService.createOrUpdateCategory(id, code, name, description, active);
            redirectAttributes.addFlashAttribute("successMessage", "Service category saved successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @GetMapping("/complaints")
    public String complaints(Model model) {
        model.addAttribute("complaints", complaintService.getAllComplaints());
        return "admin/complaints";
    }

    @PostMapping("/complaints/{id}/resolve")
    public String resolveComplaint(@PathVariable Long id,
                                   @RequestParam ComplaintStatus status,
                                   @RequestParam(required = false) String adminNotes,
                                   @RequestParam(required = false) String resolutionNotes,
                                   RedirectAttributes redirectAttributes) {
        try {
            complaintService.resolveComplaint(id, status, adminNotes, resolutionNotes);
            redirectAttributes.addFlashAttribute("successMessage", "Complaint updated to " + status);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/complaints";
    }

    // --- CSV EXPORT ENDPOINTS ---

    @GetMapping("/reports/orders.csv")
    public ResponseEntity<InputStreamResource> downloadOrdersCsv() throws IOException {
        ByteArrayInputStream in = csvReportService.exportJobOrdersToCsv();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=texsetu_orders_" + System.currentTimeMillis() + ".csv");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(new InputStreamResource(in));
    }

    @GetMapping("/reports/providers.csv")
    public ResponseEntity<InputStreamResource> downloadProvidersCsv() throws IOException {
        ByteArrayInputStream in = csvReportService.exportProvidersToCsv();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=texsetu_providers_" + System.currentTimeMillis() + ".csv");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(new InputStreamResource(in));
    }

    @GetMapping("/reports/lots.csv")
    public ResponseEntity<InputStreamResource> downloadLotsCsv() throws IOException {
        ByteArrayInputStream in = csvReportService.exportProductionLotsToCsv();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=texsetu_lots_" + System.currentTimeMillis() + ".csv");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(new InputStreamResource(in));
    }
}
