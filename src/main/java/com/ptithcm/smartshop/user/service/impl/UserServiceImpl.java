package com.ptithcm.smartshop.user.service.impl;

import com.ptithcm.smartshop.shared.exception.ConflictException;
import com.ptithcm.smartshop.shared.exception.ResourceNotFoundException;
import com.ptithcm.smartshop.security.rbac.repository.RoleRepository;
import com.ptithcm.smartshop.user.dto.UserCreateRequest;
import com.ptithcm.smartshop.user.dto.UserResponse;
import com.ptithcm.smartshop.user.dto.UserUpdateRequest;
import com.ptithcm.smartshop.security.rbac.entity.Role;
import com.ptithcm.smartshop.user.entity.User;
import com.ptithcm.smartshop.user.enums.UserStatus;
import com.ptithcm.smartshop.user.repository.UserRepository;
import com.ptithcm.smartshop.user.service.UserService;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;

	public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	@Transactional(readOnly = true)
	public List<UserResponse> findAll() {
		return userRepository.findAll().stream()
			.map(UserResponse::from)
			.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public UserResponse findById(UUID id) {
		return userRepository.findById(id)
			.map(UserResponse::from)
			.orElseThrow(() -> new ResourceNotFoundException("User not found"));
	}

	@Override
	@Transactional
	public UserResponse create(UserCreateRequest request) {
		validateCreateRequest(request);
		User user = new User();
		user.setEmail(request.email());
		user.setPhone(request.phone());
		user.setPassword(passwordEncoder.encode(request.password()));
		user.setFullName(request.fullName());
		user.setStatus(request.status() != null ? request.status() : UserStatus.ACTIVE);
		user.setRoles(resolveRoles(request.roleCodes()));
		return UserResponse.from(userRepository.save(user));
	}

	@Override
	@Transactional
	public UserResponse update(UUID id, UserUpdateRequest request) {
		User user = userRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		if (userRepository.existsByEmailAndIdNot(request.email(), id)) {
			throw new ConflictException("Email already exists");
		}
		if (userRepository.existsByPhoneAndIdNot(request.phone(), id)) {
			throw new ConflictException("Phone already exists");
		}

		user.setEmail(request.email());
		user.setPhone(request.phone());
		user.setFullName(request.fullName());
		user.setStatus(request.status() != null ? request.status() : user.getStatus());

		if (request.roleCodes() != null && !request.roleCodes().isEmpty()) {
			user.setRoles(resolveRoles(request.roleCodes()));
		}

		if (StringUtils.hasText(request.password())) {
			user.setPassword(passwordEncoder.encode(request.password()));
		}

		return UserResponse.from(userRepository.save(user));
	}

	@Override
	@Transactional
	public void delete(UUID id) {
		User user = userRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("User not found"));
		userRepository.delete(user);
	}

	private void validateCreateRequest(UserCreateRequest request) {
		if (userRepository.existsByEmail(request.email())) {
			throw new ConflictException("Email already exists");
		}
		if (userRepository.existsByPhone(request.phone())) {
			throw new ConflictException("Phone already exists");
		}
	}

	private Set<Role> resolveRoles(Set<String> roleCodes) {
		List<Role> roles = roleRepository.findByCodeIn(roleCodes);
		if (roles.size() != roleCodes.size()) {
			throw new ResourceNotFoundException("One or more roles were not found");
		}
		return new HashSet<>(roles);
	}
}

