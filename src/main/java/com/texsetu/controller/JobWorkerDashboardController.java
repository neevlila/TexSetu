package com.texsetu.controller;

import com.texsetu.dto.ProductionUpdateDto;
import com.texsetu.dto.QuotationSubmitDto;
import com.texsetu.entity.*;
import com.texsetu.entity.enums.UnitOfMeasure;
import com.texsetu.service.*;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/job-worker")
public class JobWorkerDashboardController {

    private final UserService userService;
    private final JobWorkerService jobWorkerService;
    private final CatalogService catalogService;
    private final RfqQuotationService rfqQuotationService;
    private final JobOrderService jobOrderService;
    private final ProductionLotService productionLotService;
    private final QualityInspectionService qualityInspectionService;

    public JobWorkerDashboardController(UserService userService,
                                      JobWorkerService jobWorkerService,
                                      CatalogService catalogService,
                                      RfqQuotationService rfqQuotationService,
                                      JobOrderService jobOrderService,
                                      ProductionLotService productionLotService,
                                      QualityInspectionService qualityInspectionService) {
        this.userService = userService;
        this.jobWorkerService = jobWorkerService;
        this.catalogService = catalogService;
        this.rfqQuotationService = rfqQuotationService;
        this.jobOrderService = jobOrderService;
        this.productionLotService = productionLotService;
        this.qualityInspectionService = qualityInspectionService;
    }

    private JobWorkerProfile getAuthenticatedWorker(UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found."));
        return jobWorkerService.getProfileByUserId(user.getId())
                .orElseThrow(() -> new IllegalStateException("Job worker profile not found."));
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        JobWorkerProfile worker = getAuthenticatedWorker(userDetails);
        var stats = jobWorkerService.getWorkerDashboardStats(worker.getId());
        List<JobOrder> orders = jobOrderService.getOrdersForWorker(worker.getId());
        List<Quotation> recentQuotes = rfqQuotationService.getQuotationsForWorker(worker.getId());

        model.addAttribute("worker", worker);
        model.addAttribute("stats", stats);
        model.addAttribute("orders", orders);
        model.addAttribute("recentQuotes", recentQuotes);
        return "job-worker/dashboard";
    }

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        JobWorkerProfile worker = getAuthenticatedWorker(userDetails);
        model.addAttribute("worker", worker);
        return "job-worker/profile";
    }

    @PostMapping("/profile/update-capacity")
    public String updateCapacity(@AuthenticationPrincipal UserDetails userDetails,
                                 @RequestParam BigDecimal capacityMetersPerDay,
                                 @RequestParam Integer turnaroundDays,
                                 @RequestParam String machineryDetails,
                                 RedirectAttributes redirectAttributes) {
        JobWorkerProfile worker = getAuthenticatedWorker(userDetails);
        try {
            jobWorkerService.updateCapacityAndMachinery(worker.getId(), capacityMetersPerDay, turnaroundDays, machineryDetails);
            redirectAttributes.addFlashAttribute("successMessage", "Capacity and machinery information updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/job-worker/profile";
    }

    @PostMapping("/profile/request-verification")
    public String requestVerification(@AuthenticationPrincipal UserDetails userDetails,
                                      @RequestParam(required = false) String notes,
                                      RedirectAttributes redirectAttributes) {
        JobWorkerProfile worker = getAuthenticatedWorker(userDetails);
        try {
            jobWorkerService.submitVerificationRequest(worker.getId(), notes);
            redirectAttributes.addFlashAttribute("successMessage", "Verification request submitted for admin review.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/job-worker/profile";
    }

    @GetMapping("/capabilities")
    public String capabilities(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        JobWorkerProfile worker = getAuthenticatedWorker(userDetails);
        model.addAttribute("worker", worker);
        model.addAttribute("capabilities", catalogService.getCapabilitiesForWorker(worker.getId()));
        model.addAttribute("categories", catalogService.getAllActiveCategories());
        model.addAttribute("uomList", UnitOfMeasure.values());
        return "job-worker/capabilities";
    }

    @PostMapping("/capabilities/add")
    public String addCapability(@AuthenticationPrincipal UserDetails userDetails,
                                @RequestParam Long categoryId,
                                @RequestParam String serviceName,
                                @RequestParam String description,
                                @RequestParam BigDecimal minQuantity,
                                @RequestParam UnitOfMeasure unitOfMeasure,
                                @RequestParam BigDecimal indicativePrice,
                                @RequestParam Integer leadTimeDays,
                                @RequestParam String processingDetails,
                                RedirectAttributes redirectAttributes) {
        JobWorkerProfile worker = getAuthenticatedWorker(userDetails);
        try {
            catalogService.addCapability(worker.getId(), categoryId, serviceName, description, minQuantity, unitOfMeasure, indicativePrice, leadTimeDays, processingDetails);
            redirectAttributes.addFlashAttribute("successMessage", "Service capability added to your catalog.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/job-worker/capabilities";
    }

    @PostMapping("/capabilities/{id}/delete")
    public String deleteCapability(@PathVariable Long id,
                                   @AuthenticationPrincipal UserDetails userDetails,
                                   RedirectAttributes redirectAttributes) {
        JobWorkerProfile worker = getAuthenticatedWorker(userDetails);
        try {
            catalogService.deleteCapability(id, worker.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Capability removed from your catalog.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/job-worker/capabilities";
    }

    @GetMapping("/rfqs")
    public String browseMarketRfqs(@RequestParam(required = false) Long categoryId,
                                   @AuthenticationPrincipal UserDetails userDetails,
                                   Model model) {
        JobWorkerProfile worker = getAuthenticatedWorker(userDetails);
        List<Rfq> rfqs = rfqQuotationService.getOpenRfqsForCategory(categoryId);

        model.addAttribute("worker", worker);
        model.addAttribute("rfqs", rfqs);
        model.addAttribute("categories", catalogService.getAllActiveCategories());
        model.addAttribute("selectedCategoryId", categoryId);
        return "job-worker/rfqs";
    }

    @GetMapping("/rfqs/{id}/quote")
    public String submitQuoteForm(@PathVariable Long id,
                                  @AuthenticationPrincipal UserDetails userDetails,
                                  Model model) {
        JobWorkerProfile worker = getAuthenticatedWorker(userDetails);
        Rfq rfq = rfqQuotationService.getRfqById(id)
                .orElseThrow(() -> new IllegalArgumentException("RFQ not found with ID: " + id));

        QuotationSubmitDto dto = new QuotationSubmitDto();
        dto.setRfqId(id);
        dto.setMinQuantityConditions(rfq.getQuantity());

        model.addAttribute("worker", worker);
        model.addAttribute("rfq", rfq);
        model.addAttribute("quoteDto", dto);
        return "job-worker/quote-submit";
    }

    @PostMapping("/rfqs/{id}/quote")
    public String submitQuote(@PathVariable Long id,
                              @AuthenticationPrincipal UserDetails userDetails,
                              @Valid @ModelAttribute("quoteDto") QuotationSubmitDto dto,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        JobWorkerProfile worker = getAuthenticatedWorker(userDetails);
        dto.setRfqId(id);

        if (bindingResult.hasErrors()) {
            Rfq rfq = rfqQuotationService.getRfqById(id).orElseThrow();
            model.addAttribute("worker", worker);
            model.addAttribute("rfq", rfq);
            return "job-worker/quote-submit";
        }

        try {
            Quotation quote = rfqQuotationService.submitQuotation(worker, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Quotation " + quote.getQuoteNumber() + " submitted successfully to the buyer.");
            return "redirect:/job-worker/quotes";
        } catch (Exception e) {
            Rfq rfq = rfqQuotationService.getRfqById(id).orElseThrow();
            model.addAttribute("worker", worker);
            model.addAttribute("rfq", rfq);
            model.addAttribute("errorMessage", e.getMessage());
            return "job-worker/quote-submit";
        }
    }

    @GetMapping("/quotes")
    public String myQuotes(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        JobWorkerProfile worker = getAuthenticatedWorker(userDetails);
        model.addAttribute("worker", worker);
        model.addAttribute("quotes", rfqQuotationService.getQuotationsForWorker(worker.getId()));
        return "job-worker/quotes";
    }

    @GetMapping("/orders")
    public String myOrders(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        JobWorkerProfile worker = getAuthenticatedWorker(userDetails);
        model.addAttribute("worker", worker);
        model.addAttribute("orders", jobOrderService.getOrdersForWorker(worker.getId()));
        return "job-worker/orders";
    }

    @GetMapping("/orders/{id}")
    public String orderDetails(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model) {
        JobWorkerProfile worker = getAuthenticatedWorker(userDetails);
        JobOrder order = jobOrderService.getOrderById(id)
                .orElseThrow(() -> new IllegalArgumentException("Job order not found with ID: " + id));

        if (!order.getJobWorker().getId().equals(worker.getId())) {
            throw new SecurityException("Unauthorized access to order.");
        }

        ProductionLot lot = productionLotService.getLotByOrderId(order.getId()).orElse(null);
        List<ProductionEvent> timeline = (lot != null) ? productionLotService.getTimelineForLot(lot.getId()) : List.of();
        List<QualityInspection> inspections = qualityInspectionService.getInspectionsForOrder(order.getId());

        model.addAttribute("worker", worker);
        model.addAttribute("order", order);
        model.addAttribute("lot", lot);
        model.addAttribute("timeline", timeline);
        model.addAttribute("inspections", inspections);
        model.addAttribute("updateDto", new ProductionUpdateDto());
        return "job-worker/order-details";
    }

    @PostMapping("/orders/{id}/update-stage")
    public String updateOrderStage(@PathVariable Long id,
                                   @AuthenticationPrincipal UserDetails userDetails,
                                   @Valid @ModelAttribute("updateDto") ProductionUpdateDto dto,
                                   RedirectAttributes redirectAttributes) {
        User actor = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        dto.setOrderId(id);

        try {
            jobOrderService.updateOrderStatus(actor, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Production stage updated to " + dto.getNextStatus() + ". Event recorded in Passport.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/job-worker/orders/" + id;
    }

    @PostMapping("/orders/{id}/reconcile-material")
    public String reconcileMaterial(@PathVariable Long id,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    @RequestParam BigDecimal receivedQuantity,
                                    @RequestParam(required = false) String notes,
                                    RedirectAttributes redirectAttributes) {
        User actor = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        JobOrder order = jobOrderService.getOrderById(id).orElseThrow();
        ProductionLot lot = productionLotService.getLotByOrderId(order.getId()).orElseThrow();

        try {
            productionLotService.reconcileReceivedMaterial(lot.getId(), receivedQuantity, notes, actor);
            redirectAttributes.addFlashAttribute("successMessage", "Material quantity verified & reconciled: " + receivedQuantity + " " + order.getUnitOfMeasure());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/job-worker/orders/" + id;
    }
}
