package com.ptithcm.smartshop.user.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthLoginRequest(
	@NotBlank(message = "Email or phone is required")
	String emailOrPhone,
	@NotBlank(message = "Password is required")
	String password
) {
}
