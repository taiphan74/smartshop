package com.ptithcm.smartshop.user.repository;

import com.ptithcm.smartshop.user.entity.Role;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, UUID> {

	@EntityGraph(attributePaths = "permissions")
	Optional<Role> findByCode(String code);

	@EntityGraph(attributePaths = "permissions")
	List<Role> findByCodeIn(Collection<String> codes);
}
