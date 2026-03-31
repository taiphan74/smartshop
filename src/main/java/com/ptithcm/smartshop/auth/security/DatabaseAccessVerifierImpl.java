package com.ptithcm.smartshop.auth.security;

import com.ptithcm.smartshop.auth.enums.AuthPermission;
import com.ptithcm.smartshop.auth.enums.AuthRole;
import com.ptithcm.smartshop.user.repository.UserRepository;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class DatabaseAccessVerifierImpl implements DatabaseAccessVerifier {

	private final UserRepository userRepository;

	public DatabaseAccessVerifierImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public boolean hasPermission(UUID userId, AuthPermission permission) {
		return userRepository.existsByIdAndPermissionCode(userId, permission.code());
	}

	@Override
	public boolean hasRole(UUID userId, AuthRole role) {
		return userRepository.existsByIdAndRoleCode(userId, role.code());
	}
}
