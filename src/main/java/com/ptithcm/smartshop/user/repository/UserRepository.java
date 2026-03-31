package com.ptithcm.smartshop.user.repository;

import com.ptithcm.smartshop.user.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

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

}
