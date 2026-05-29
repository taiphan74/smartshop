package com.ptithcm.smartshop.admin.controller;

import com.ptithcm.smartshop.admin.service.AdminOrderManagementService;
import com.ptithcm.smartshop.order.domain.entity.Order;
import com.ptithcm.smartshop.order.domain.enums.OrderStatus;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    private final AdminOrderManagementService orderManagementService;

    public AdminOrderController(AdminOrderManagementService orderManagementService) {
        this.orderManagementService = orderManagementService;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public String list(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        model.addAttribute("orders", orderManagementService.list(
                status,
                PageRequest.of(Math.max(page, 0), 20, Sort.by(Sort.Direction.DESC, "createdAt"))));
        model.addAttribute("selectedStatus", status);
        model.addAttribute("statuses", OrderStatus.values());
        return "admin/orders/list";
    }

    @GetMapping("/{orderId}")
    @Transactional(readOnly = true)
    public String detail(@PathVariable UUID orderId, Model model) {
        Order order = orderManagementService.get(orderId);
        model.addAttribute("order", order);
        model.addAttribute("nextStatuses", orderManagementService.allowedNextStatuses(order.getStatus()));
        return "admin/orders/detail";
    }

    @PostMapping("/{orderId}/status")
    public String changeStatus(
            @PathVariable UUID orderId,
            @RequestParam OrderStatus status,
            RedirectAttributes redirectAttributes) {
        try {
            orderManagementService.changeStatus(orderId, status);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật trạng thái đơn hàng");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/orders/" + orderId;
    }
}
