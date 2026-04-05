package com.ptithcm.smartshop.security.rbac;

import com.ptithcm.smartshop.auth.enums.AuthRole;
import java.util.Collection;
import java.util.UUID;

public interface AuthorizationService {

	boolean hasPermission(UUID userId, Permission permission);

	boolean hasAnyPermission(UUID userId, Collection<Permission> permissions);

	boolean hasAllPermissions(UUID userId, Collection<Permission> permissions);

	boolean hasRole(UUID userId, AuthRole role);
}
