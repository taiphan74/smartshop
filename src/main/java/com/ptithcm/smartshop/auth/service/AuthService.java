package com.ptithcm.smartshop.auth.service;

import com.ptithcm.smartshop.auth.dto.AuthLoginRequest;
import com.ptithcm.smartshop.auth.dto.AuthRegisterRequest;
import com.ptithcm.smartshop.auth.dto.AuthResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {

	AuthResponse register(AuthRegisterRequest request);

	AuthResponse login(AuthLoginRequest request, HttpServletRequest httpServletRequest);

	void logout(HttpServletRequest httpServletRequest);

	AuthResponse me(HttpServletRequest httpServletRequest);
}
