package com.ptithcm.smartshop.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthLoginRequest(
	@NotBlank(message = "Email or phone is required")
	String emailOrPhone,
	@NotBlank(message = "Password is required")
	String password
) {
}
