package com.ptithcm.smartshop.auth.dto;

import com.ptithcm.smartshop.security.session.SessionUser;
import com.ptithcm.smartshop.user.dto.UserResponse;

/**
 * DTO phản hồi cho các API/luồng auth.
 *
 * @param message     thông điệp kết quả nghiệp vụ
 * @param sessionId   mã phiên hiện tại (nếu có)
 * @param sessionUser thông tin user tối giản lưu trong session
 * @param user        thông tin user chi tiết trả về cho UI/client
 */
public record AuthResponse(
		String message,
		String sessionId,
		SessionUser sessionUser,
		UserResponse user) {
}
