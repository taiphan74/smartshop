package com.ptithcm.smartshop.user.repository;

import com.ptithcm.smartshop.user.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, UUID> {

	boolean existsByEmail(String email);

	boolean existsByPhone(String phone);

	boolean existsByEmailAndIdNot(String email, UUID id);

	boolean existsByPhoneAndIdNot(String phone, UUID id);

	@Override
	@EntityGraph(attributePaths = {"profile", "userRoles", "userRoles.role", "userRoles.role.permissions"})
	List<User> findAll();

	@Override
	@EntityGraph(attributePaths = {"profile", "userRoles", "userRoles.role", "userRoles.role.permissions"})
	Optional<User> findById(UUID id);

	@EntityGraph(attributePaths = {"profile", "userRoles", "userRoles.role", "userRoles.role.permissions"})
	Optional<User> findByEmail(String email);

	@EntityGraph(attributePaths = {"profile", "userRoles", "userRoles.role", "userRoles.role.permissions"})
	Optional<User> findByPhone(String phone);

	@EntityGraph(attributePaths = {"profile", "userRoles", "userRoles.role", "userRoles.role.permissions"})
	default Optional<User> findByEmailOrPhoneWithRoles(String emailOrPhone) {
		Optional<User> byEmail = findByEmail(emailOrPhone);
		return byEmail.isPresent() ? byEmail : findByPhone(emailOrPhone);
	}

	@Query("""
		select count(u) > 0
		from User u
		join u.userRoles ur
		join ur.role r
		join r.permissions p
		where u.id = :userId
		and p.code = :permissionCode
	""")
	boolean existsByIdAndPermissionCode(@Param("userId") UUID userId, @Param("permissionCode") String permissionCode);

	@Query("""
		select count(u) > 0
		from User u
		join u.userRoles ur
		join ur.role r
		where u.id = :userId
		and r.code = :roleCode
	""")
	boolean existsByIdAndRoleCode(@Param("userId") UUID userId, @Param("roleCode") String roleCode);
}
