package com.ptithcm.smartshop.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RequestRegistrationOtpDto(
		@NotBlank(message = "Email is required") @Email(message = "Email is invalid") String email) {
}
