package com.ptithcm.smartshop.security.principal;

import com.ptithcm.smartshop.user.entity.Permission;
import com.ptithcm.smartshop.user.entity.Role;
import com.ptithcm.smartshop.user.entity.User;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class CustomUserDetails implements UserDetails {

	private final UUID id;
	private final String username;
	private final String password;
	private final boolean enabled;
	private final Set<GrantedAuthority> authorities;

	public CustomUserDetails(UUID id, String username, String password, boolean enabled, Set<GrantedAuthority> authorities) {
		this.id = id;
		this.username = username;
		this.password = password;
		this.enabled = enabled;
		this.authorities = authorities;
	}

	public static CustomUserDetails from(User user) {
		Set<GrantedAuthority> grantedAuthorities = new LinkedHashSet<>();
		for (Role role : user.getRoles()) {
			grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + role.getCode()));
			for (Permission permission : role.getPermissions()) {
				grantedAuthorities.add(new SimpleGrantedAuthority(permission.getCode()));
			}
		}
		return new CustomUserDetails(user.getId(), user.getUsername(), user.getPassword(), user.isEnabled(), grantedAuthorities);
	}

	public UUID getId() {
		return id;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}
}

