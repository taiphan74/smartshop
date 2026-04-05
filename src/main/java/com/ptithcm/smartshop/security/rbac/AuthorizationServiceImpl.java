package com.ptithcm.smartshop.security.rbac;

import com.ptithcm.smartshop.auth.enums.AuthRole;
import com.ptithcm.smartshop.security.rbac.repository.PermissionRepository;
import com.ptithcm.smartshop.security.rbac.repository.RoleRepository;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationServiceImpl implements AuthorizationService {

	private final PermissionRepository permissionRepository;
	private final RoleRepository roleRepository;

	public AuthorizationServiceImpl(PermissionRepository permissionRepository, RoleRepository roleRepository) {
		this.permissionRepository = permissionRepository;
		this.roleRepository = roleRepository;
	}

	@Override
	public boolean hasPermission(UUID userId, Permission permission) {
		return permissionRepository.hasPermission(userId, permission.code());
	}

	@Override
	public boolean hasAnyPermission(UUID userId, Collection<Permission> permissions) {
		if (permissions == null || permissions.isEmpty()) {
			return true;
		}
		return permissionRepository.hasAnyPermission(userId, toCodes(permissions));
	}

	@Override
	public boolean hasAllPermissions(UUID userId, Collection<Permission> permissions) {
		if (permissions == null || permissions.isEmpty()) {
			return true;
		}
		return permissionRepository.countGrantedPermissions(userId, toCodes(permissions)) == permissions.stream()
			.filter(Objects::nonNull)
			.map(Permission::code)
			.distinct()
			.count();
	}

	@Override
	public boolean hasRole(UUID userId, AuthRole role) {
		return roleRepository.hasRole(userId, role.code());
	}

	private Collection<String> toCodes(Collection<Permission> permissions) {
		return permissions.stream()
			.filter(Objects::nonNull)
			.map(Permission::code)
			.distinct()
			.collect(Collectors.toList());
	}
}
