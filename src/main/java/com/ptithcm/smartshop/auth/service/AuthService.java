package com.ptithcm.smartshop.auth.service;

import com.ptithcm.smartshop.auth.dto.AuthLoginRequest;
import com.ptithcm.smartshop.auth.dto.AuthRegisterRequest;
import com.ptithcm.smartshop.auth.dto.AuthResponse;
import com.ptithcm.smartshop.user.dto.UserResponse;

public interface AuthService {

    AuthResponse register(AuthRegisterRequest request);

    AuthResponse login(AuthLoginRequest request);

    UserResponse activateUser(String email);
}
