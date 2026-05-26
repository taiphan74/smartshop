package com.ptithcm.smartshop.seller.controller;

import com.ptithcm.smartshop.seller.service.SellerDashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/seller")
public class SellerDashboardController {

    private final SellerDashboardService sellerDashboardService;

    public SellerDashboardController(SellerDashboardService sellerDashboardService) {
        this.sellerDashboardService = sellerDashboardService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Tạm thời dùng mock UUID để kiểm tra khả năng render giao diện layout
        UUID mockShopId = UUID.randomUUID(); 
        Map<String, Object> metrics = sellerDashboardService.getDashboardMetrics(mockShopId);
        
        model.addAttribute("metrics", metrics);
        return "seller/dashboard";
    }
}
