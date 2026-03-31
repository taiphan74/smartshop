package com.ptithcm.smartshop.user.controller;

import com.ptithcm.smartshop.common.exception.ConflictException;
import com.ptithcm.smartshop.security.SessionConstants;
import com.ptithcm.smartshop.user.dto.AuthLoginRequest;
import com.ptithcm.smartshop.user.dto.AuthRegisterRequest;
import com.ptithcm.smartshop.user.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@GetMapping("/login")
	public String loginPage(Model model, HttpServletRequest request) {
		if (hasActiveSession(request)) {
			return "redirect:/";
		}
		if (!model.containsAttribute("loginRequest")) {
			model.addAttribute("loginRequest", new AuthLoginRequest("", ""));
		}
		return "auth/login";
	}

	@PostMapping("/login")
	public String login(
		@Valid @ModelAttribute("loginRequest") AuthLoginRequest request,
		BindingResult bindingResult,
		HttpServletRequest httpServletRequest,
		Model model
	) {
		if (bindingResult.hasErrors()) {
			return "auth/login";
		}

		try {
			authService.login(request, httpServletRequest);
			return "redirect:/";
		} catch (AuthenticationException exception) {
			model.addAttribute("formError", "Email/so dien thoai hoac mat khau khong dung");
			return "auth/login";
		}
	}

	@GetMapping("/register")
	public String registerPage(Model model, HttpServletRequest request) {
		if (hasActiveSession(request)) {
			return "redirect:/";
		}
		if (!model.containsAttribute("registerRequest")) {
			model.addAttribute("registerRequest", new AuthRegisterRequest("", "", "", ""));
		}
		return "auth/register";
	}

	@PostMapping("/register")
	public String register(
		@Valid @ModelAttribute("registerRequest") AuthRegisterRequest request,
		BindingResult bindingResult,
		HttpServletRequest httpServletRequest,
		Model model,
		RedirectAttributes redirectAttributes
	) {
		if (bindingResult.hasErrors()) {
			return "auth/register";
		}

		try {
			authService.register(request);
			authService.login(new AuthLoginRequest(request.email(), request.password()), httpServletRequest);
			redirectAttributes.addFlashAttribute("successMessage", "Dang ky thanh cong");
			return "redirect:/";
		} catch (ConflictException exception) {
			model.addAttribute("formError", exception.getMessage());
			return "auth/register";
		} catch (AuthenticationException exception) {
			model.addAttribute("formError", "Dang ky thanh cong nhung khong the dang nhap tu dong");
			return "auth/login";
		}
	}

	@PostMapping("/logout")
	public String logout(HttpServletRequest httpServletRequest) {
		authService.logout(httpServletRequest);
		return "redirect:/auth/login?logout";
	}

	private boolean hasActiveSession(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		return session != null && session.getAttribute(SessionConstants.CURRENT_USER) != null;
	}
}
