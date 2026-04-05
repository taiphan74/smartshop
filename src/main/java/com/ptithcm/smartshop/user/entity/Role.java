package com.ptithcm.smartshop.user.entity;

import com.ptithcm.smartshop.shared.entity.AuditableEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "roles")
public class Role extends AuditableEntity {

	@Column(nullable = false, unique = true, length = 50)
	private String code;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(length = 255)
	private String description;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
		name = "role_permissions",
		joinColumns = @JoinColumn(name = "role_id"),
		inverseJoinColumns = @JoinColumn(name = "permission_id")
	)
	private Set<Permission> permissions = new LinkedHashSet<>();

	@OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<UserRole> userRoles = new LinkedHashSet<>();

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Set<Permission> getPermissions() {
		return permissions;
	}

	public void setPermissions(Set<Permission> permissions) {
		this.permissions = permissions;
	}

	public Set<UserRole> getUserRoles() {
		return userRoles;
	}

	public Set<User> getUsers() {
		return userRoles.stream()
			.map(UserRole::getUser)
			.collect(Collectors.toCollection(LinkedHashSet::new));
	}
}

