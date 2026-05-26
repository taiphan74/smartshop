package com.ptithcm.smartshop.seller.controller;

import com.ptithcm.smartshop.seller.service.SellerDashboardService;
import com.ptithcm.smartshop.security.principal.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.Map;

@Controller
@RequestMapping("/seller")
public class SellerDashboardController {

    private final SellerDashboardService sellerDashboardService;

    public SellerDashboardController(SellerDashboardService sellerDashboardService) {
        this.sellerDashboardService = sellerDashboardService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }
        Map<String, Object> metrics = sellerDashboardService.getDashboardMetrics(userDetails.getId());
        model.addAttribute("metrics", metrics);
        return "seller/dashboard";
    }
}
