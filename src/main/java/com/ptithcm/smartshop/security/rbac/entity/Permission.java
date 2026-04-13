package com.ptithcm.smartshop.security.rbac.entity;

import com.ptithcm.smartshop.shared.entity.BaseUuidEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "permissions")
public class Permission extends BaseUuidEntity {

	@Column(nullable = false, unique = true, length = 100)
	private String code;

	@Column(nullable = false, length = 255)
	private String description;

	@ManyToMany(mappedBy = "permissions")
	private Set<Role> roles = new LinkedHashSet<>();

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Set<Role> getRoles() {
		return roles;
	}
}
