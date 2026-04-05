package com.ptithcm.smartshop.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO nhận dữ liệu đăng ký tài khoản mới.
 *
 * @param email    email duy nhất của người dùng
 * @param phone    số điện thoại duy nhất của người dùng
 * @param password mật khẩu đầu vào, sẽ được mã hóa trước khi lưu
 * @param fullName họ tên hiển thị của người dùng
 */
public record AuthRegisterRequest(
		@NotBlank(message = "Email is required") @Email(message = "Email is invalid") String email,
		@NotBlank(message = "Phone is required") @Size(min = 10, max = 20, message = "Phone must be between 10 and 20 characters") String phone,
		@NotBlank(message = "Password is required") @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters") String password,
		@NotBlank(message = "Full name is required") @Size(max = 150, message = "Full name must not exceed 150 characters") String fullName) {
}
