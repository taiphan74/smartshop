package com.ptithcm.smartshop.auth.security;

import com.ptithcm.smartshop.auth.annotation.RequirePermission;
import com.ptithcm.smartshop.auth.annotation.RequireRole;
import com.ptithcm.smartshop.common.exception.UnauthorizedException;
import com.ptithcm.smartshop.security.CustomUserDetails;
import java.util.UUID;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuthorizationAspect {

	private final DatabaseAccessVerifier databaseAccessVerifier;

	public AuthorizationAspect(DatabaseAccessVerifier databaseAccessVerifier) {
		this.databaseAccessVerifier = databaseAccessVerifier;
	}

	@Before("@within(requirePermission) || @annotation(requirePermission)")
	public void checkPermission(JoinPoint joinPoint, RequirePermission requirePermission) {
		if (requirePermission == null) {
			requirePermission = joinPoint.getTarget().getClass().getAnnotation(RequirePermission.class);
		}
		if (requirePermission == null) {
			return;
		}

		UUID userId = getCurrentUserId();
		if (!databaseAccessVerifier.hasPermission(userId, requirePermission.value())) {
			throw new AccessDeniedException("Permission denied: " + requirePermission.value().code());
		}
	}

	@Before("@within(requireRole) || @annotation(requireRole)")
	public void checkRole(JoinPoint joinPoint, RequireRole requireRole) {
		if (requireRole == null) {
			requireRole = joinPoint.getTarget().getClass().getAnnotation(RequireRole.class);
		}
		if (requireRole == null) {
			return;
		}

		UUID userId = getCurrentUserId();
		if (!databaseAccessVerifier.hasRole(userId, requireRole.value())) {
			throw new AccessDeniedException("Role denied: " + requireRole.value().code());
		}
	}

	private UUID getCurrentUserId() {
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
