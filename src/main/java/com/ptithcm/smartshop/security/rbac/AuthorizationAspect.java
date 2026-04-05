package com.ptithcm.smartshop.security.rbac;

import com.ptithcm.smartshop.auth.annotation.RequireRole;
import com.ptithcm.smartshop.shared.exception.UnauthorizedException;
import com.ptithcm.smartshop.security.principal.CustomUserDetails;
import java.util.Arrays;
import java.util.List;
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

	private final AuthorizationService authorizationService;

	public AuthorizationAspect(AuthorizationService authorizationService) {
		this.authorizationService = authorizationService;
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
		List<Permission> requiredPermissions = Arrays.stream(requirePermission.value()).toList();
		List<Permission> anyOfPermissions = Arrays.stream(requirePermission.anyOf()).toList();
		List<Permission> allOfPermissions = Arrays.stream(requirePermission.allOf()).toList();
		if (requiredPermissions.isEmpty() && anyOfPermissions.isEmpty() && allOfPermissions.isEmpty()) {
			throw new IllegalStateException("@RequirePermission must declare value, anyOf, or allOf");
		}
		if (!authorizationService.hasAllPermissions(userId, requiredPermissions)) {
			throw new AccessDeniedException("Permission denied: required permissions " + requiredPermissions);
		}
		if (!authorizationService.hasAllPermissions(userId, allOfPermissions)) {
			throw new AccessDeniedException("Permission denied: allOf permissions " + allOfPermissions);
		}
		if (!anyOfPermissions.isEmpty() && !authorizationService.hasAnyPermission(userId, anyOfPermissions)) {
			throw new AccessDeniedException("Permission denied: anyOf permissions " + anyOfPermissions);
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
		if (!authorizationService.hasRole(userId, requireRole.value())) {
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


