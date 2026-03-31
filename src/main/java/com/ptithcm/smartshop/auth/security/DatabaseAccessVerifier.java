package com.ptithcm.smartshop.auth.security;

import com.ptithcm.smartshop.auth.enums.AuthPermission;
import com.ptithcm.smartshop.auth.enums.AuthRole;
import java.util.UUID;

public interface DatabaseAccessVerifier {

	boolean hasPermission(UUID userId, AuthPermission permission);

	boolean hasRole(UUID userId, AuthRole role);
}
