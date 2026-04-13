package com.ptithcm.smartshop.user.dto;

import com.ptithcm.smartshop.user.enums.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record UserUpdateRequest(
	@NotBlank(message = "Email is required")
	@Email(message = "Email is invalid")
	String email,
	@NotBlank(message = "Phone is required")
	@Size(min = 10, max = 20, message = "Phone must be between 10 and 20 characters")
	String phone,
	@NotBlank(message = "Full name is required")
	@Size(max = 150, message = "Full name must not exceed 150 characters")
	String fullName,
	UserStatus status,
	Set<String> roleCodes,
	@Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
	String password
) {
}
