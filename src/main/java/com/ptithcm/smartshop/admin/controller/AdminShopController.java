package com.ptithcm.smartshop.admin.controller;

import com.ptithcm.smartshop.admin.dto.AdminShopForm;
import com.ptithcm.smartshop.admin.service.AdminShopApprovalService;
import com.ptithcm.smartshop.shop.entity.Shop;
import jakarta.validation.Valid;
import com.ptithcm.smartshop.security.principal.CustomUserDetails;
import com.ptithcm.smartshop.shop.enums.ShopStatus;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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

	@GetMapping("/{shopId}")
	public String detail(@PathVariable UUID shopId, Model model) {
		model.addAttribute("shop", adminShopApprovalService.getShop(shopId));
		model.addAttribute("history", adminShopApprovalService.history(shopId));
		return "admin/shops/detail";
	}

	@GetMapping("/{shopId}/edit")
	public String edit(@PathVariable UUID shopId, Model model) {
		Shop shop = adminShopApprovalService.getShop(shopId);
		AdminShopForm form = new AdminShopForm();
		form.setName(shop.getName());
		form.setSlug(shop.getSlug());
		form.setLogoUrl(shop.getLogoUrl());
		form.setBannerUrl(shop.getBannerUrl());
		form.setEmail(shop.getEmail());
		form.setPhone(shop.getPhone());
		form.setDescription(shop.getDescription());
		form.setStatus(shop.getStatus());
		model.addAttribute("shop", shop);
		model.addAttribute("form", form);
		model.addAttribute("statuses", ShopStatus.values());
		return "admin/shops/form";
	}

	@PostMapping("/{shopId}/edit")
	public String update(
			@PathVariable UUID shopId,
			@Valid AdminShopForm form,
			BindingResult bindingResult,
			Model model,
			RedirectAttributes redirectAttributes) {
		if (bindingResult.hasErrors()) {
			model.addAttribute("shop", adminShopApprovalService.getShop(shopId));
			model.addAttribute("statuses", ShopStatus.values());
			return "admin/shops/form";
		}
		try {
			adminShopApprovalService.updateShop(shopId, form);
			redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật shop");
			return "redirect:/admin/shops/" + shopId;
		} catch (IllegalArgumentException ex) {
			bindingResult.reject("shop", ex.getMessage());
			model.addAttribute("shop", adminShopApprovalService.getShop(shopId));
			model.addAttribute("statuses", ShopStatus.values());
			return "admin/shops/form";
		}
	}

	@PostMapping("/{shopId}/delete")
	public String delete(@PathVariable UUID shopId, RedirectAttributes redirectAttributes) {
		try {
			adminShopApprovalService.deleteShop(shopId);
			redirectAttributes.addFlashAttribute("successMessage", "Đã xóa shop");
			return "redirect:/admin/shops";
		} catch (IllegalArgumentException ex) {
			redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
			return "redirect:/admin/shops/" + shopId;
		}
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
