package com.ptithcm.smartshop.auth.controller;

import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ptithcm.smartshop.auth.dto.AuthLoginRequest;
import com.ptithcm.smartshop.auth.dto.AuthRegisterRequest;
import com.ptithcm.smartshop.auth.service.AuthService;
import com.ptithcm.smartshop.security.session.SessionConstants;
import com.ptithcm.smartshop.shared.exception.ConflictException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

/**
 * Controller xử lý luồng xác thực phía giao diện web (Thymeleaf):
 * hiển thị form login/register, nhận submit và điều hướng sau xử lý.
 */
@Controller
@RequestMapping("/auth")
public class AuthController {

	/**
	 * Service chứa nghiệp vụ xác thực và quản lý phiên.
	 */
	private final AuthService authService;

	/**
	 * Khởi tạo controller với dependency nghiệp vụ auth.
	 */
	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	/**
	 * Hiển thị trang đăng nhập.
	 *
	 * Luồng xử lý:
	 * 1) Nếu đã có phiên hợp lệ thì chuyển hướng về trang chủ.
	 * 2) Nếu chưa có dữ liệu form trong model thì khởi tạo object rỗng để bind.
	 * 3) Trả về template đăng nhập.
	 */
	@GetMapping("/login")
	public String loginPage(Model model, HttpServletRequest request) {
		// Bước 1: chặn người dùng đã đăng nhập truy cập lại form login.
		if (hasActiveSession(request)) {
			return "redirect:/";
		}
		// Bước 2: tạo model mặc định cho form nếu chưa có dữ liệu trước đó.
		if (!model.containsAttribute("loginRequest")) {
			model.addAttribute("loginRequest", new AuthLoginRequest("", ""));
		}
		// Bước 3: render giao diện login.
		return "auth/login";
	}

	/**
	 * Xử lý submit form đăng nhập.
	 *
	 * Luồng xử lý:
	 * 1) Validate dữ liệu đầu vào; nếu sai trả lại form để hiển thị lỗi field.
	 * 2) Gọi service để xác thực và tạo phiên đăng nhập.
	 * 3) Nếu thành công chuyển hướng về trang chủ.
	 * 4) Nếu xác thực thất bại thì trả thông báo lỗi tổng quát lên form.
	 */
	@PostMapping("/login")
	public String login(
			@Valid @ModelAttribute("loginRequest") AuthLoginRequest request,
			BindingResult bindingResult,
			HttpServletRequest httpServletRequest,
			Model model) {
		// Bước 1: dừng sớm khi dữ liệu không hợp lệ.
		if (bindingResult.hasErrors()) {
			return "auth/login";
		}

		try {
			// Bước 2: xác thực thông tin đăng nhập và gắn session/security context.
			authService.login(request, httpServletRequest);
			// Bước 3: điều hướng về trang chủ nếu đăng nhập thành công.
			return "redirect:/";
		} catch (AuthenticationException exception) {
			// Bước 4: ẩn chi tiết lý do thất bại để tránh lộ thông tin nhạy cảm.
			model.addAttribute("formError", "Email/so dien thoai hoac mat khau khong dung");
			return "auth/login";
		}
	}

	/**
	 * Hiển thị trang đăng ký.
	 *
	 * Luồng xử lý:
	 * 1) Nếu đã đăng nhập thì chuyển hướng về trang chủ.
	 * 2) Nếu model chưa có dữ liệu đăng ký thì khởi tạo object rỗng.
	 * 3) Trả về template đăng ký.
	 */
	@GetMapping("/register")
	public String registerPage(Model model, HttpServletRequest request) {
		// Bước 1: người dùng đã đăng nhập không cần vào trang register.
		if (hasActiveSession(request)) {
			return "redirect:/";
		}
		// Bước 2: tạo model mặc định cho form đăng ký.
		if (!model.containsAttribute("registerRequest")) {
			model.addAttribute("registerRequest", new AuthRegisterRequest("", "", "", ""));
		}
		// Bước 3: render giao diện register.
		return "auth/register";
	}

	/**
	 * Xử lý submit form đăng ký.
	 *
	 * Luồng xử lý:
	 * 1) Validate dữ liệu đầu vào; nếu lỗi thì hiển thị lại form.
	 * 2) Tạo tài khoản mới qua service.
	 * 3) Tự động đăng nhập bằng thông tin vừa đăng ký.
	 * 4) Gắn flash message thành công và chuyển hướng về trang chủ.
	 * 5) Nếu trùng email/số điện thoại thì hiển thị lỗi business trên form.
	 * 6) Nếu đăng ký thành công nhưng đăng nhập tự động thất bại thì điều hướng
	 * sang login.
	 */
	@PostMapping("/register")
	public String register(
			@Valid @ModelAttribute("registerRequest") AuthRegisterRequest request,
			BindingResult bindingResult,
			HttpServletRequest httpServletRequest,
			Model model,
			RedirectAttributes redirectAttributes) {
		// Bước 1: phản hồi sớm khi dữ liệu không hợp lệ.
		if (bindingResult.hasErrors()) {
			return "auth/register";
		}

		try {
			// Bước 2: tạo tài khoản mới.
			authService.register(request);
			// Bước 3: tự động đăng nhập ngay sau đăng ký.
			authService.login(new AuthLoginRequest(request.email(), request.password()), httpServletRequest);
			// Bước 4: thêm thông báo thành công ở request kế tiếp.
			redirectAttributes.addFlashAttribute("successMessage", "Dang ky thanh cong");
			return "redirect:/";
		} catch (ConflictException exception) {
			// Bước 5: hiển thị lỗi nghiệp vụ (trùng email/phone) cho người dùng.
			model.addAttribute("formError", exception.getMessage());
			return "auth/register";
		} catch (AuthenticationException exception) {
			// Bước 6: fallback sang trang login nếu auto-login không thành công.
			model.addAttribute("formError", "Dang ky thanh cong nhung khong the dang nhap tu dong");
			return "auth/login";
		}
	}

	/**
	 * Xử lý đăng xuất người dùng hiện tại.
	 *
	 * Luồng xử lý:
	 * 1) Gọi service để hủy session và dọn security context.
	 * 2) Chuyển hướng về trang login kèm cờ logout.
	 */
	@PostMapping("/logout")
	public String logout(HttpServletRequest httpServletRequest) {
		// Bước 1: hủy phiên đăng nhập hiện tại.
		authService.logout(httpServletRequest);
		// Bước 2: điều hướng về trang login.
		return "redirect:/auth/login?logout";
	}

	/**
	 * Kiểm tra request hiện tại có session đăng nhập hợp lệ hay không.
	 *
	 * @return true nếu tồn tại session và có thông tin CURRENT_USER.
	 */
	private boolean hasActiveSession(HttpServletRequest request) {
		// Bước 1: lấy session hiện hữu, không tạo mới nếu chưa có.
		HttpSession session = request.getSession(false);
		// Bước 2: xác nhận session chứa marker người dùng đã đăng nhập.
		return session != null && session.getAttribute(SessionConstants.CURRENT_USER) != null;
	}
}
