package com.ptithcm.smartshop.user.controller;

import com.ptithcm.smartshop.user.dto.AuthResponse;
import com.ptithcm.smartshop.user.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

	private final AuthService authService;

	public HomeController(AuthService authService) {
		this.authService = authService;
	}

	@GetMapping("/")
	public String home(HttpServletRequest request, Model model) {
		AuthResponse authResponse = authService.me(request);
		model.addAttribute("auth", authResponse);
		model.addAttribute("sessionUser", authResponse.sessionUser());
		model.addAttribute("user", authResponse.user());
		return "home";
	}
}
