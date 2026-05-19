package com.ptithcm.smartshop.admin.controller;

import com.ptithcm.smartshop.admin.service.AdminDashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminDashboardController {

	private final AdminDashboardService adminDashboardService;

	public AdminDashboardController(AdminDashboardService adminDashboardService) {
		this.adminDashboardService = adminDashboardService;
	}

	@GetMapping({"/admin", "/admin/", "/admin/dashboard"})
	public String index(Model model) {
		model.addAttribute("dashboard", adminDashboardService.getDashboard());
		return "admin/dashboard";
	}
}
