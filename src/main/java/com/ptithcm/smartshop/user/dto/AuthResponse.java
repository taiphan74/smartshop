package com.ptithcm.smartshop.user.dto;

import com.ptithcm.smartshop.security.SessionUser;

public record AuthResponse(
	String message,
	String sessionId,
	SessionUser sessionUser,
	UserResponse user
) {
}
