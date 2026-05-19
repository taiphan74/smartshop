package com.ptithcm.smartshop.admin.controller;

import com.ptithcm.smartshop.admin.service.AdminShopApprovalService;
import com.ptithcm.smartshop.security.principal.CustomUserDetails;
import com.ptithcm.smartshop.shop.enums.ShopStatus;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/shops")
public class AdminShopController {

	private final AdminShopApprovalService adminShopApprovalService;

	public AdminShopController(AdminShopApprovalService adminShopApprovalService) {
		this.adminShopApprovalService = adminShopApprovalService;
	}

	@GetMapping
	public String list(@RequestParam(required = false) ShopStatus status, Model model) {
		model.addAttribute("shops", adminShopApprovalService.listShops(status));
		model.addAttribute("selectedStatus", status);
		model.addAttribute("statuses", ShopStatus.values());
		return "admin/shops/list";
	}

	@PostMapping("/{shopId}/approve")
	public String approve(
			@PathVariable UUID shopId,
			@AuthenticationPrincipal CustomUserDetails principal,
			RedirectAttributes redirectAttributes) {
		try {
			adminShopApprovalService.approve(shopId, principal.getId());
			redirectAttributes.addFlashAttribute("successMessage", "Đã duyệt shop");
		} catch (IllegalArgumentException ex) {
			redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
		}
		return "redirect:/admin/shops";
	}

	@PostMapping("/{shopId}/reject")
	public String reject(
			@PathVariable UUID shopId,
			@RequestParam String reason,
			@AuthenticationPrincipal CustomUserDetails principal,
			RedirectAttributes redirectAttributes) {
		try {
			adminShopApprovalService.reject(shopId, principal.getId(), reason);
			redirectAttributes.addFlashAttribute("successMessage", "Đã từ chối shop");
		} catch (IllegalArgumentException ex) {
			redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
		}
		return "redirect:/admin/shops";
	}

	@PostMapping("/{shopId}/suspend")
	public String suspend(
			@PathVariable UUID shopId,
			@RequestParam String reason,
			@AuthenticationPrincipal CustomUserDetails principal,
			RedirectAttributes redirectAttributes) {
		try {
			adminShopApprovalService.suspend(shopId, principal.getId(), reason);
			redirectAttributes.addFlashAttribute("successMessage", "Đã tạm ngưng shop");
		} catch (IllegalArgumentException ex) {
			redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
		}
		return "redirect:/admin/shops";
	}
}
