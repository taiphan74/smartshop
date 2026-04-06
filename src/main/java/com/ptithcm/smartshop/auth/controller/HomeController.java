package com.ptithcm.smartshop.auth.controller;

import com.ptithcm.smartshop.auth.dto.AuthResponse;
import com.ptithcm.smartshop.auth.service.AuthService;
import com.ptithcm.smartshop.product.dto.ProductListDTO;
import com.ptithcm.smartshop.product.dto.CategoryDTO;
import com.ptithcm.smartshop.product.dto.PageResponse;
import com.ptithcm.smartshop.product.service.ProductService;
import com.ptithcm.smartshop.product.service.CategoryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

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
	private final ProductService productService;
	private final CategoryService categoryService;

	public HomeController(AuthService authService, ProductService productService, CategoryService categoryService) {
		this.authService = authService;
		this.productService = productService;
		this.categoryService = categoryService;
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
		AuthResponse authResponse = authService.me(request);
		if (authResponse != null) {
			model.addAttribute("auth", authResponse);
			model.addAttribute("sessionUser", authResponse.sessionUser());
			model.addAttribute("user", authResponse.user());
		}

		// Lấy tất cả danh mục gốc (parent is null) cho section Danh mục ở trang chủ
		PageResponse<CategoryDTO> categoryPage = categoryService.findParentCategories(0, 50, "name", "asc"); 
		List<CategoryDTO> rootCategories = categoryPage.getContent();
		model.addAttribute("rootCategories", rootCategories);

		// Tạo danh sách thu gọn (tối đa 8 mục) dành riêng cho thanh menu Header để tránh tràn
		model.addAttribute("headerCategories", rootCategories.stream().limit(8).toList());

		// Lấy danh sách sản phẩm để hiển thị trên trang chủ
		List<ProductListDTO> products = productService.findAllProducts();
		model.addAttribute("products", products);

		return "home";
	}
}
