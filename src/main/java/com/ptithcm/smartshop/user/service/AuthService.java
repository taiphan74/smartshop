package com.ptithcm.smartshop.user.service;

import com.ptithcm.smartshop.user.dto.AuthLoginRequest;
import com.ptithcm.smartshop.user.dto.AuthRegisterRequest;
import com.ptithcm.smartshop.user.dto.AuthResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {

	AuthResponse register(AuthRegisterRequest request);

	AuthResponse login(AuthLoginRequest request, HttpServletRequest httpServletRequest);

	void logout(HttpServletRequest httpServletRequest);

	AuthResponse me(HttpServletRequest httpServletRequest);
}
