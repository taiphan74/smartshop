package com.ptithcm.smartshop.auth.service;

import com.ptithcm.smartshop.auth.dto.AuthLoginRequest;
import com.ptithcm.smartshop.auth.dto.AuthRegisterRequest;
import com.ptithcm.smartshop.auth.dto.AuthResponse;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Contract nghiệp vụ xác thực và quản lý phiên đăng nhập.
 */
public interface AuthService {

	/**
	 * Đăng ký tài khoản mới.
	 *
	 * @param request dữ liệu đăng ký
	 * @return thông tin kết quả đăng ký
	 */
	AuthResponse register(AuthRegisterRequest request);

	/**
	 * Đăng nhập và khởi tạo session/security context.
	 *
	 * @param request            dữ liệu đăng nhập
	 * @param httpServletRequest request hiện tại để gắn session
	 * @return thông tin phiên sau đăng nhập thành công
	 */
	AuthResponse login(AuthLoginRequest request, HttpServletRequest httpServletRequest);

	/**
	 * Đăng xuất người dùng hiện tại.
	 *
	 * @param httpServletRequest request hiện tại để hủy session
	 */
	void logout(HttpServletRequest httpServletRequest);

	/**
	 * Lấy thông tin phiên hiện tại của người dùng đã đăng nhập.
	 *
	 * @param httpServletRequest request hiện tại
	 * @return thông tin auth tương ứng phiên hiện tại
	 */
	AuthResponse me(HttpServletRequest httpServletRequest);
}
