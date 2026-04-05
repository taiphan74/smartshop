package com.ptithcm.smartshop.security.rbac.repository;

import com.ptithcm.smartshop.user.entity.Role;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoleRepository extends JpaRepository<Role, UUID> {

	@EntityGraph(attributePaths = "permissions")
	Optional<Role> findByCode(String code);

	@EntityGraph(attributePaths = "permissions")
	List<Role> findByCodeIn(Collection<String> codes);

	@Query("""
		select count(r) > 0
		from Role r
		join r.userRoles ur
		where ur.user.id = :userId
		and r.code = :roleCode
	""")
	boolean hasRole(@Param("userId") UUID userId, @Param("roleCode") String roleCode);
}
