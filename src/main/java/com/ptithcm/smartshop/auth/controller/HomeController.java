package com.ptithcm.smartshop.auth.controller;

import com.ptithcm.smartshop.auth.dto.AuthResponse;
import com.ptithcm.smartshop.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller trang chủ.
 *
 * Vai trò chính:
 * - Lấy thông tin phiên hiện tại từ auth service.
 * - Đưa dữ liệu người dùng lên model để render giao diện home.
 */
@Controller
public class HomeController {

	/**
	 * Service nghiệp vụ xác thực để truy xuất người dùng hiện tại.
	 */
	private final AuthService authService;

	/**
	 * Khởi tạo controller với dependency auth service.
	 */
	public HomeController(AuthService authService) {
		this.authService = authService;
	}

	/**
	 * Render trang chủ.
	 *
	 * Luồng xử lý:
	 * 1) Lấy thông tin auth hiện tại từ session/request.
	 * 2) Đưa dữ liệu auth/sessionUser/user vào model cho view sử dụng.
	 * 3) Trả về template home.
	 */
	@GetMapping("/")
	public String home(HttpServletRequest request, Model model) {
		// Bước 1: lấy snapshot thông tin phiên đăng nhập hiện tại.
		AuthResponse authResponse = authService.me(request);
		// Bước 2: publish các object cần thiết cho Thymeleaf template.
		model.addAttribute("auth", authResponse);
		model.addAttribute("sessionUser", authResponse.sessionUser());
		model.addAttribute("user", authResponse.user());
		// Bước 3: render trang chủ.
		return "home";
	}
}
