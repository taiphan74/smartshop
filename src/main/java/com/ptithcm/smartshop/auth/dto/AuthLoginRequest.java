package com.ptithcm.smartshop.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO nhận dữ liệu đăng nhập từ client.
 *
 * @param emailOrPhone email hoặc số điện thoại dùng để xác thực
 * @param password     mật khẩu dạng plain text trước khi gửi vào
 *                     AuthenticationManager
 */
public record AuthLoginRequest(
		@NotBlank(message = "Email or phone is required") String emailOrPhone,
		@NotBlank(message = "Password is required") String password) {
}
