package com.ptithcm.smartshop.user.dto;

import com.ptithcm.smartshop.user.entity.Permission;
import com.ptithcm.smartshop.user.entity.Role;
import com.ptithcm.smartshop.user.entity.User;
import com.ptithcm.smartshop.user.enums.UserStatus;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UserResponse(
	UUID id,
	String email,
	String phone,
	String fullName,
	UserStatus status,
	Set<String> roles,
	Set<String> permissions,
	Instant createdAt,
	Instant updatedAt
) {

	public static UserResponse from(User user) {
		Set<String> roleNames = user.getRoles().stream()
			.map(Role::getCode)
			.collect(java.util.stream.Collectors.toUnmodifiableSet());
		Set<String> permissionCodes = user.getRoles().stream()
			.flatMap(role -> role.getPermissions().stream())
			.map(Permission::getCode)
			.collect(java.util.stream.Collectors.toUnmodifiableSet());
		return new UserResponse(
			user.getId(),
			user.getEmail(),
			user.getPhone(),
			user.getFullName(),
			user.getStatus(),
			roleNames,
			permissionCodes,
			user.getCreatedAt(),
			user.getUpdatedAt()
		);
	}
}
