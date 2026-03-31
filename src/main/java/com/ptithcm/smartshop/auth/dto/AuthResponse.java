package com.ptithcm.smartshop.auth.dto;

import com.ptithcm.smartshop.security.SessionUser;
import com.ptithcm.smartshop.user.dto.UserResponse;

public record AuthResponse(
	String message,
	String sessionId,
	SessionUser sessionUser,
	UserResponse user
) {
}
