package com.ptithcm.smartshop.product.controller;

import com.ptithcm.smartshop.product.dto.ProductDetailDTO;
import com.ptithcm.smartshop.product.dto.request.ProductRequest;
import com.ptithcm.smartshop.product.service.ProductService;
import com.ptithcm.smartshop.security.rbac.Permission;
import com.ptithcm.smartshop.security.rbac.RequirePermission;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
/**
 * Controller quản trị sản phẩm cho seller/shop manager.
 *
 * Trách nhiệm chính:
 * - Tạo mới sản phẩm cho shop được chọn.
 * - Cập nhật thông tin sản phẩm hiện có.
 * - Xóa sản phẩm khi user hiện tại có đủ quyền và thuộc shop tương ứng.
 */
public class ProductController {

	private final ProductService productService;

	public ProductController(ProductService productService) {
		this.productService = productService;
	}

	/**
	 * Tạo mới sản phẩm cho shop được chọn trong request.
	 */
	@PostMapping
	@RequirePermission(Permission.PRODUCT_CREATE)
	public ResponseEntity<ProductDetailDTO> create(@Valid @RequestBody ProductRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(productService.save(request));
	}

	/**
	 * Cập nhật sản phẩm hiện có.
	 */
	@PutMapping("/{id}")
	@RequirePermission(Permission.PRODUCT_UPDATE)
	public ResponseEntity<ProductDetailDTO> update(@PathVariable String id, @Valid @RequestBody ProductRequest request) {
		return ResponseEntity.ok(productService.update(id, request));
	}

	/**
	 * Xóa sản phẩm theo id.
	 */
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@RequirePermission(Permission.PRODUCT_DELETE)
	public void delete(@PathVariable String id) {
		productService.deleteById(id);
	}
}
