package com.ptithcm.smartshop.user.entity;

import com.ptithcm.smartshop.security.rbac.entity.Role;
import com.ptithcm.smartshop.shared.entity.AuditableEntity;
import com.ptithcm.smartshop.shop.entity.Shop;
import com.ptithcm.smartshop.shop.entity.ShopUser;
import com.ptithcm.smartshop.user.enums.UserStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
public class User extends AuditableEntity {

	@Column(nullable = false, unique = true, length = 150)
	private String email;

	@Column(nullable = true, unique = true, length = 20)
	private String phone;

	@Column(name = "password_hash", nullable = false, length = 255)
	private String passwordHash;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private UserStatus status = UserStatus.PENDING;

	@OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private UserProfile profile;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<UserRole> userRoles = new LinkedHashSet<>();

	@OneToMany(mappedBy = "owner")
	private Set<Shop> ownedShops = new LinkedHashSet<>();

	@OneToMany(mappedBy = "user")
	private Set<ShopUser> shopUsers = new LinkedHashSet<>();

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public UserStatus getStatus() {
		return status;
	}

	public void setStatus(UserStatus status) {
		this.status = status;
	}

	public UserProfile getProfile() {
		return profile;
	}

	public void setProfile(UserProfile profile) {
		this.profile = profile;
		if (profile != null) {
			profile.setUser(this);
		}
	}

	public Set<UserRole> getUserRoles() {
		return userRoles;
	}

	public void setUserRoles(Set<UserRole> userRoles) {
		this.userRoles.clear();
		if (userRoles != null) {
			userRoles.forEach(this::addUserRole);
		}
	}

	public Set<Role> getRoles() {
		return userRoles.stream()
			.map(UserRole::getRole)
			.filter(Objects::nonNull)
			.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	public void setRoles(Set<Role> roles) {
		this.userRoles.clear();
		if (roles != null) {
			roles.forEach(this::addRole);
		}
	}

	public void addRole(Role role) {
		UserRole userRole = new UserRole();
		userRole.setUser(this);
		userRole.setRole(role);
		this.userRoles.add(userRole);
	}

	public void addUserRole(UserRole userRole) {
		userRole.setUser(this);
		this.userRoles.add(userRole);
	}

	public String getPassword() {
		return passwordHash;
	}

	public void setPassword(String password) {
		this.passwordHash = password;
	}

	public boolean isEnabled() {
		return status == UserStatus.ACTIVE;
	}

	public void setEnabled(boolean enabled) {
		this.status = enabled ? UserStatus.ACTIVE : UserStatus.BLOCKED;
	}

	public String getUsername() {
		return email;
	}

	public void setUsername(String username) {
		this.email = username;
	}

	public String getFullName() {
		return profile != null ? profile.getFullName() : null;
	}

	public void setFullName(String fullName) {
		if (this.profile == null) {
			UserProfile userProfile = new UserProfile();
			userProfile.setFullName(fullName);
			setProfile(userProfile);
			return;
		}
		this.profile.setFullName(fullName);
	}

	public Set<Shop> getOwnedShops() {
		return ownedShops;
	}

	public Set<ShopUser> getShopUsers() {
		return shopUsers;
	}
}

