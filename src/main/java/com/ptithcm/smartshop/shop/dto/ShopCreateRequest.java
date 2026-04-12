package com.ptithcm.smartshop.shop.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ShopCreateRequest(
	@NotBlank(message = "Tên shop không được để trống")
	@Size(max = 150, message = "Tên shop không được vượt quá 150 ký tự")
	String name,
	@Size(max = 160, message = "Mã shop không được vượt quá 160 ký tự")
	String slug,
	@Email(message = "Email shop không hợp lệ")
	@Size(max = 150, message = "Email shop không được vượt quá 150 ký tự")
	String email,
	@Size(max = 20, message = "Số điện thoại shop không được vượt quá 20 ký tự")
	String phone,
	@Size(max = 500, message = "Logo URL không được vượt quá 500 ký tự")
	String logoUrl,
	@Size(max = 500, message = "Banner URL không được vượt quá 500 ký tự")
	String bannerUrl,
	String description
) {
}
