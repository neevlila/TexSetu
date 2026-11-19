package com.texsetu.controller;

import com.texsetu.dto.ComplaintCreateDto;
import com.texsetu.dto.InspectionCreateDto;
import com.texsetu.dto.ReviewCreateDto;
import com.texsetu.dto.RfqCreateDto;
import com.texsetu.entity.*;
import com.texsetu.entity.enums.OrderStatus;
import com.texsetu.payment.PaymentGateway;
import com.texsetu.service.*;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/buyer")
public class BuyerDashboardController {

    private final UserService userService;
    private final RfqQuotationService rfqQuotationService;
    private final JobOrderService jobOrderService;
    private final ProductionLotService productionLotService;
    private final QualityInspectionService qualityInspectionService;
    private final CatalogService catalogService;
    private final PaymentGateway paymentGateway;
    private final ReviewService reviewService;
    private final ComplaintService complaintService;

    public BuyerDashboardController(UserService userService,
                                  RfqQuotationService rfqQuotationService,
                                  JobOrderService jobOrderService,
                                  ProductionLotService productionLotService,
                                  QualityInspectionService qualityInspectionService,
                                  CatalogService catalogService,
                                  PaymentGateway paymentGateway,
                                  ReviewService reviewService,
                                  ComplaintService complaintService) {
        this.userService = userService;
        this.rfqQuotationService = rfqQuotationService;
        this.jobOrderService = jobOrderService;
        this.productionLotService = productionLotService;
        this.qualityInspectionService = qualityInspectionService;
        this.catalogService = catalogService;
        this.paymentGateway = paymentGateway;
        this.reviewService = reviewService;
        this.complaintService = complaintService;
    }

    private User getAuthenticatedUser(UserDetails userDetails) {
        return userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found."));
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User buyer = getAuthenticatedUser(userDetails);
        List<Rfq> rfqs = rfqQuotationService.getRfqsForBuyer(buyer.getId());
        List<JobOrder> orders = jobOrderService.getOrdersForBuyer(buyer.getId());

        long activeOrders = orders.stream().filter(o -> o.getCurrentStatus() != OrderStatus.COMPLETED && o.getCurrentStatus() != OrderStatus.DELIVERED && o.getCurrentStatus() != OrderStatus.CANCELLED).count();
        long completedOrders = orders.stream().filter(o -> o.getCurrentStatus() == OrderStatus.COMPLETED || o.getCurrentStatus() == OrderStatus.DELIVERED).count();

        model.addAttribute("buyer", buyer);
        model.addAttribute("rfqs", rfqs);
        model.addAttribute("orders", orders);
        model.addAttribute("totalRfqs", rfqs.size());
        model.addAttribute("activeOrders", activeOrders);
        model.addAttribute("completedOrders", completedOrders);
        return "buyer/dashboard";
    }

    @GetMapping("/rfqs")
    public String myRfqs(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User buyer = getAuthenticatedUser(userDetails);
        model.addAttribute("rfqs", rfqQuotationService.getRfqsForBuyer(buyer.getId()));
        return "buyer/rfqs";
    }

    @GetMapping("/rfqs/new")
    public String newRfqForm(Model model) {
        if (!model.containsAttribute("rfqDto")) {
            model.addAttribute("rfqDto", new RfqCreateDto());
        }
        model.addAttribute("categories", catalogService.getAllActiveCategories());
        return "buyer/rfq-create";
    }

    @PostMapping("/rfqs/new")
    public String createRfq(@AuthenticationPrincipal UserDetails userDetails,
                            @Valid @ModelAttribute("rfqDto") RfqCreateDto dto,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes,
                            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", catalogService.getAllActiveCategories());
            return "buyer/rfq-create";
        }

        try {
            User buyer = getAuthenticatedUser(userDetails);
            Rfq rfq = rfqQuotationService.createRfq(buyer, dto);
            redirectAttributes.addFlashAttribute("successMessage", "RFQ " + rfq.getRfqNumber() + " submitted successfully.");
            return "redirect:/buyer/rfqs";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("categories", catalogService.getAllActiveCategories());
            return "buyer/rfq-create";
        }
    }

    @GetMapping("/rfqs/{id}/quotes")
    public String compareQuotations(@PathVariable Long id,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    Model model) {
        User buyer = getAuthenticatedUser(userDetails);
        Rfq rfq = rfqQuotationService.getRfqById(id)
                .orElseThrow(() -> new IllegalArgumentException("RFQ not found with ID: " + id));

        if (!rfq.getBuyer().getId().equals(buyer.getId())) {
            throw new SecurityException("Unauthorized access to RFQ quotations.");
        }

        List<Quotation> quotes = rfqQuotationService.getQuotationsForRfq(id);

        model.addAttribute("rfq", rfq);
        model.addAttribute("quotes", quotes);
        return "buyer/quote-compare";
    }

    @PostMapping("/quotes/{id}/accept")
    public String acceptQuotation(@PathVariable Long id,
                                  @AuthenticationPrincipal UserDetails userDetails,
                                  RedirectAttributes redirectAttributes) {
        User buyer = getAuthenticatedUser(userDetails);
        try {
            JobOrder order = rfqQuotationService.acceptQuotation(id, buyer);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Quotation accepted! Job Order " + order.getOrderNumber() + " created. Digital Production Lot Passport is now active.");
            return "redirect:/buyer/orders/" + order.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/buyer/rfqs";
        }
    }

    @GetMapping("/orders")
    public String myOrders(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User buyer = getAuthenticatedUser(userDetails);
        model.addAttribute("orders", jobOrderService.getOrdersForBuyer(buyer.getId()));
        return "buyer/orders";
    }

    @GetMapping("/orders/{id}")
    public String orderDetails(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model) {
        User buyer = getAuthenticatedUser(userDetails);
        JobOrder order = jobOrderService.getOrderById(id)
                .orElseThrow(() -> new IllegalArgumentException("Job order not found with ID: " + id));

        if (!order.getBuyer().getId().equals(buyer.getId())) {
            throw new SecurityException("Unauthorized access to this order.");
        }

        ProductionLot lot = productionLotService.getLotByOrderId(order.getId()).orElse(null);
        List<ProductionEvent> timeline = (lot != null) ? productionLotService.getTimelineForLot(lot.getId()) : List.of();
        List<QualityInspection> inspections = qualityInspectionService.getInspectionsForOrder(order.getId());

        model.addAttribute("order", order);
        model.addAttribute("lot", lot);
        model.addAttribute("timeline", timeline);
        model.addAttribute("inspections", inspections);
        model.addAttribute("inspectionDto", new InspectionCreateDto());
        model.addAttribute("complaintDto", new ComplaintCreateDto());
        model.addAttribute("reviewDto", new ReviewCreateDto());
        return "buyer/order-details";
    }

    @PostMapping("/orders/{id}/inspect")
    public String submitInspection(@PathVariable Long id,
                                   @AuthenticationPrincipal UserDetails userDetails,
                                   @Valid @ModelAttribute("inspectionDto") InspectionCreateDto dto,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes) {
        User buyer = getAuthenticatedUser(userDetails);
        dto.setOrderId(id);

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Validation error: Check inspected quantities and remarks.");
            return "redirect:/buyer/orders/" + id;
        }

        try {
            qualityInspectionService.recordInspection(buyer, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Quality inspection result recorded: " + dto.getResult());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/buyer/orders/" + id;
    }

    @PostMapping("/orders/{id}/confirm-delivery")
    public String confirmDelivery(@PathVariable Long id,
                                  @AuthenticationPrincipal UserDetails userDetails,
                                  RedirectAttributes redirectAttributes) {
        User buyer = getAuthenticatedUser(userDetails);
        JobOrder order = jobOrderService.getOrderById(id)
                .orElseThrow(() -> new IllegalArgumentException("Job order not found"));

        if (!order.getBuyer().getId().equals(buyer.getId())) {
            throw new SecurityException("Unauthorized access.");
        }

        try {
            com.texsetu.dto.ProductionUpdateDto dto = new com.texsetu.dto.ProductionUpdateDto();
            dto.setOrderId(id);
            dto.setNextStatus(OrderStatus.DELIVERED);
            dto.setStageName("FINAL_DELIVERY_CONFIRMED");
            dto.setDescription("Buyer confirmed physical receipt of processed goods at destination.");
            jobOrderService.updateOrderStatus(buyer, dto);

            redirectAttributes.addFlashAttribute("successMessage", "Order marked as DELIVERED successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/buyer/orders/" + id;
    }

    @PostMapping("/orders/{id}/pay-simulate")
    public String simulatePayment(@PathVariable Long id,
                                  @AuthenticationPrincipal UserDetails userDetails,
                                  RedirectAttributes redirectAttributes) {
        User buyer = getAuthenticatedUser(userDetails);
        JobOrder order = jobOrderService.getOrderById(id)
                .orElseThrow(() -> new IllegalArgumentException("Job order not found"));

        if (!order.getBuyer().getId().equals(buyer.getId())) {
            throw new SecurityException("Unauthorized access.");
        }

        PaymentRecord record = paymentGateway.initiateSimulatedPayment(order, buyer, order.getAgreedPrice());
        paymentGateway.completeSimulatedPayment(record.getTransactionReference(), true);

        redirectAttributes.addFlashAttribute("successMessage",
                "Payment of INR " + order.getAgreedPrice() + " simulated successfully via TexSetu Mock Gateway (Ref: " + record.getTransactionReference() + ").");
        return "redirect:/buyer/orders/" + id;
    }

    @PostMapping("/orders/{id}/complain")
    public String submitComplaint(@PathVariable Long id,
                                  @AuthenticationPrincipal UserDetails userDetails,
                                  @Valid @ModelAttribute("complaintDto") ComplaintCreateDto dto,
                                  RedirectAttributes redirectAttributes) {
        User buyer = getAuthenticatedUser(userDetails);
        dto.setOrderId(id);

        try {
            Complaint complaint = complaintService.lodgeComplaint(buyer, dto);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Complaint registered with reference " + complaint.getComplaintReference() + ". TexSetu Support will review it.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/buyer/orders/" + id;
    }

    @PostMapping("/orders/{id}/review")
    public String submitReview(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails userDetails,
                               @Valid @ModelAttribute("reviewDto") ReviewCreateDto dto,
                               RedirectAttributes redirectAttributes) {
        User buyer = getAuthenticatedUser(userDetails);
        dto.setOrderId(id);

        try {
            reviewService.submitReview(buyer, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Review submitted successfully! Thank you for your feedback.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/buyer/orders/" + id;
    }
}
