package com.ptithcm.smartshop.auth.controller;

import com.ptithcm.smartshop.auth.dto.AuthLoginRequest;
import com.ptithcm.smartshop.auth.dto.AuthRegisterRequest;
import com.ptithcm.smartshop.auth.service.AuthService;
import com.ptithcm.smartshop.auth.service.RegistrationOtpService;
import com.ptithcm.smartshop.security.principal.CustomUserDetails;
import com.ptithcm.smartshop.security.session.SessionConstants;
import com.ptithcm.smartshop.security.session.SessionUser;
import com.ptithcm.smartshop.shared.exception.ConflictException;
import com.ptithcm.smartshop.user.entity.User;
import com.ptithcm.smartshop.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class AuthController {

	private final AuthService authService;
	private final RegistrationOtpService registrationOtpService;
	private final UserRepository userRepository;

	public AuthController(AuthService authService, RegistrationOtpService registrationOtpService, UserRepository userRepository) {
		this.authService = authService;
		this.registrationOtpService = registrationOtpService;
		this.userRepository = userRepository;
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
			Model model) {
		if (bindingResult.hasErrors()) {
			return "auth/login";
		}

		try {
			authService.login(request, httpServletRequest);
			return "redirect:/";
		} catch (DisabledException exception) {
			model.addAttribute("formError", "Bạn chưa xác thực tài khoản, vui lòng xác thực tài khoản.");
			return "auth/login";
		} catch (AuthenticationException exception) {
			model.addAttribute("formError", "Email hoặc mật khẩu không đúng");
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
			RedirectAttributes redirectAttributes) {
		if (bindingResult.hasErrors()) {
			return "auth/register";
		}

		try {
			authService.register(request);
			redirectAttributes.addFlashAttribute("successMessage", "Đăng ký thành công, vui lòng kiểm tra email để xác thực tài khoản.");
			return "redirect:/auth/login";
		} catch (DisabledException exception) {
			redirectAttributes.addFlashAttribute("successMessage", "Đăng ký thành công, vui lòng xác thực tài khoản.");
			return "redirect:/auth/login";
		} catch (ConflictException exception) {
			model.addAttribute("formError", exception.getMessage());
			return "auth/register";
		} catch (AuthenticationException exception) {
			model.addAttribute("formError", "Đăng ký thành công nhưng không thể đăng nhập tự động");
			return "auth/login";
		}
	}

	@GetMapping("/verify")
	public String verifyPage(
			@RequestParam(name = "otp", required = false) String otp,
			Model model,
			HttpServletRequest request) {
		if (hasActiveSession(request)) {
			return "redirect:/";
		}
		if (otp != null && !otp.isBlank()) {
			model.addAttribute("otpCode", otp);
		}
		return "auth/verify";
	}

	@PostMapping("/verify")
	public String verify(
			@RequestParam("otp") String otp,
			HttpServletRequest httpServletRequest,
			Model model,
			RedirectAttributes redirectAttributes) {
		try {
			com.ptithcm.smartshop.user.dto.UserResponse userResp = registrationOtpService.activateByOtp(otp, UUID.randomUUID().toString());
			User user = userRepository.findByEmail(userResp.email()).orElseThrow();
			org.springframework.security.core.Authentication auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
					CustomUserDetails.from(user), null, user.getRoles().stream().map(r -> new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + r.getCode())).toList());
			org.springframework.security.core.context.SecurityContext context = org.springframework.security.core.context.SecurityContextHolder.createEmptyContext();
			context.setAuthentication(auth);
			org.springframework.security.core.context.SecurityContextHolder.setContext(context);
			HttpSession session = httpServletRequest.getSession(true);
			session.setAttribute(org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
			session.setAttribute(SessionConstants.CURRENT_USER, SessionUser.from(user));
			redirectAttributes.addFlashAttribute("successMessage", "Xác thực tài khoản thành công");
			return "redirect:/";
		} catch (ConflictException exception) {
			model.addAttribute("formError", exception.getMessage());
			model.addAttribute("otpCode", otp);
			return "auth/verify";
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
