package com.ptithcm.smartshop.security.impl;

import com.ptithcm.smartshop.security.CurrentUserService;
import com.ptithcm.smartshop.security.principal.CustomUserDetails;
import com.ptithcm.smartshop.shared.exception.UnauthorizedException;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserServiceImpl implements CurrentUserService {

	@Override
	public UUID getCurrentUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new UnauthorizedException("Authentication is required");
		}
		Object principal = authentication.getPrincipal();
		if (!(principal instanceof CustomUserDetails customUserDetails)) {
			throw new UnauthorizedException("Authentication is required");
		}
		return customUserDetails.getId();
	}
}
