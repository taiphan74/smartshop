package com.ptithcm.smartshop.shop.controller;

import com.ptithcm.smartshop.security.rbac.Permission;
import com.ptithcm.smartshop.security.rbac.RequirePermission;
import com.ptithcm.smartshop.shop.dto.ShopCreateRequest;
import com.ptithcm.smartshop.shop.dto.ShopResponse;
import com.ptithcm.smartshop.shop.service.ShopService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shops")
/**
 * Controller quản lý shop cho API.
 *
 * Trách nhiệm chính:
 * - Tạo shop mới cho user hiện tại khi có đủ permission.
 * - Trả danh sách shop mà user hiện tại sở hữu/tham gia.
 * - Trả thông tin public của shop theo slug.
 */
public class ShopController {

	private final ShopService shopService;

	public ShopController(ShopService shopService) {
		this.shopService = shopService;
	}

	/**
	 * Lấy danh sách shop mà user hiện tại có thể quản lý/tham gia.
	 */
	@GetMapping("/me")
	@RequirePermission(Permission.SHOP_READ)
	public ResponseEntity<List<ShopResponse>> findMyShops() {
		return ResponseEntity.ok(shopService.findMyShops());
	}

	/**
	 * Lấy thông tin public của shop theo slug.
	 */
	@GetMapping("/{slug}")
	public ResponseEntity<ShopResponse> findBySlug(@PathVariable String slug) {
		return ResponseEntity.ok(shopService.findBySlug(slug));
	}

	/**
	 * Tạo mới shop cho user hiện tại.
	 */
	@PostMapping
	@RequirePermission(Permission.SHOP_CREATE)
	public ResponseEntity<ShopResponse> create(@Valid @RequestBody ShopCreateRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(shopService.create(request));
	}
}
