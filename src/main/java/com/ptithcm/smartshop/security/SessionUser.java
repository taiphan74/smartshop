package com.ptithcm.smartshop.security;

import com.ptithcm.smartshop.user.entity.Role;
import com.ptithcm.smartshop.user.entity.User;
import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

public record SessionUser(
	UUID id,
	String email,
	String phone,
	String fullName,
	Set<String> roles
) implements Serializable {

	public static SessionUser from(User user) {
		Set<String> roleNames = user.getRoles().stream()
			.map(Role::getCode)
			.collect(java.util.stream.Collectors.toUnmodifiableSet());
		return new SessionUser(user.getId(), user.getEmail(), user.getPhone(), user.getFullName(), roleNames);
	}
}
