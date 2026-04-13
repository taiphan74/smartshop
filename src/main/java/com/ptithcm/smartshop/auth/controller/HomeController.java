package com.ptithcm.smartshop.auth.controller;

import com.ptithcm.smartshop.auth.dto.AuthResponse;
import com.ptithcm.smartshop.auth.service.AuthService;
import com.ptithcm.smartshop.banner.service.BannerService;
import com.ptithcm.smartshop.shared.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller trang chủ.
 *
 * Vai trò chính:
 * - Hiển thị trang Home cho cả guest và user đã đăng nhập.
 * - Khi đã login: hiển thị thông tin session.
 * - Khi chưa login: vẫn hiển thị trang Home bình thường (không throw lỗi).
 */
@Controller
public class HomeController {

	private final AuthService authService;
	private final BannerService bannerService;

	public HomeController(AuthService authService, BannerService bannerService) {
		this.authService = authService;
		this.bannerService = bannerService;
	}

	/**
	 * Render trang chủ.
	 *
	 * Luồng xử lý:
	 * 1) Thử lấy thông tin auth từ session.
	 * 2) Nếu chưa đăng nhập → bỏ qua, guest vẫn xem được trang chủ.
	 * 3) Nếu đã đăng nhập → đưa thông tin user lên model.
	 * 4) Trả về template home.
	 */
	@GetMapping("/")
	public String home(HttpServletRequest request, Model model) {
		try {
			AuthResponse authResponse = authService.me(request);
			model.addAttribute("auth", authResponse);
			model.addAttribute("sessionUser", authResponse.sessionUser());
			model.addAttribute("user", authResponse.user());
		} catch (UnauthorizedException e) {
			// Guest chưa đăng nhập → vẫn hiển thị trang Home bình thường.
			// Không đưa thông tin user vào model, template tự xử lý trường hợp null.
		}
		model.addAttribute("banners", bannerService.findAllActive());
		return "home";
	}
}
