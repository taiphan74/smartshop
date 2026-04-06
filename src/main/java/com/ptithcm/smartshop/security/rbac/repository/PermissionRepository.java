package com.ptithcm.smartshop.security.rbac.repository;

import com.ptithcm.smartshop.security.rbac.entity.Permission;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {

	Optional<Permission> findByCode(String code);

	List<Permission> findByCodeIn(Collection<String> codes);

	@Query("""
		select count(p) > 0
		from Permission p
		join p.roles r
		join r.userRoles ur
		where ur.user.id = :userId
		and p.code = :permissionCode
	""")
	boolean hasPermission(@Param("userId") UUID userId, @Param("permissionCode") String permissionCode);

	@Query("""
		select count(distinct p)
		from Permission p
		join p.roles r
		join r.userRoles ur
		where ur.user.id = :userId
		and p.code in :permissionCodes
	""")
	long countGrantedPermissions(@Param("userId") UUID userId, @Param("permissionCodes") Collection<String> permissionCodes);

	@Query("""
		select count(p) > 0
		from Permission p
		join p.roles r
		join r.userRoles ur
		where ur.user.id = :userId
		and p.code in :permissionCodes
	""")
	boolean hasAnyPermission(@Param("userId") UUID userId, @Param("permissionCodes") Collection<String> permissionCodes);
}
