package com.ptithcm.smartshop.auth.service.impl;

import com.ptithcm.smartshop.auth.dto.AuthLoginRequest;
import com.ptithcm.smartshop.auth.dto.AuthRegisterRequest;
import com.ptithcm.smartshop.auth.dto.AuthResponse;
import com.ptithcm.smartshop.auth.service.AuthService;
import com.ptithcm.smartshop.common.exception.ConflictException;
import com.ptithcm.smartshop.common.exception.ResourceNotFoundException;
import com.ptithcm.smartshop.common.exception.UnauthorizedException;
import com.ptithcm.smartshop.security.CustomUserDetails;
import com.ptithcm.smartshop.security.SessionConstants;
import com.ptithcm.smartshop.security.SessionUser;
import com.ptithcm.smartshop.user.dto.UserResponse;
import com.ptithcm.smartshop.user.entity.Role;
import com.ptithcm.smartshop.user.entity.User;
import com.ptithcm.smartshop.user.enums.UserStatus;
import com.ptithcm.smartshop.user.repository.RoleRepository;
import com.ptithcm.smartshop.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Set;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

	private static final String CUSTOMER_ROLE_CODE = "CUSTOMER";

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;

	public AuthServiceImpl(
		UserRepository userRepository,
		RoleRepository roleRepository,
		PasswordEncoder passwordEncoder,
		AuthenticationManager authenticationManager
	) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
		this.authenticationManager = authenticationManager;
	}

	@Override
	@Transactional
	public AuthResponse register(AuthRegisterRequest request) {
		if (userRepository.existsByEmail(request.email())) {
			throw new ConflictException("Email already exists");
		}
		if (userRepository.existsByPhone(request.phone())) {
			throw new ConflictException("Phone already exists");
		}

		Role customerRole = roleRepository.findByCode(CUSTOMER_ROLE_CODE)
			.orElseThrow(() -> new ResourceNotFoundException("Default role CUSTOMER was not found"));

		User user = new User();
		user.setEmail(request.email());
		user.setPhone(request.phone());
		user.setPassword(passwordEncoder.encode(request.password()));
		user.setFullName(request.fullName());
		user.setStatus(UserStatus.ACTIVE);
		user.setRoles(Set.of(customerRole));

		User savedUser = userRepository.save(user);
		return new AuthResponse("Register successful", null, null, UserResponse.from(savedUser));
	}

	@Override
	@Transactional(readOnly = true)
	public AuthResponse login(AuthLoginRequest request, HttpServletRequest httpServletRequest) {
		Authentication authentication = authenticationManager.authenticate(
			new UsernamePasswordAuthenticationToken(request.emailOrPhone(), request.password())
		);

		CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
		User user = userRepository.findById(principal.getId())
			.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context);

		HttpSession session = httpServletRequest.getSession(true);
		session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
		SessionUser sessionUser = SessionUser.from(user);
		session.setAttribute(SessionConstants.CURRENT_USER, sessionUser);

		return new AuthResponse("Login successful", session.getId(), sessionUser, UserResponse.from(user));
	}

	@Override
	public void logout(HttpServletRequest httpServletRequest) {
		HttpSession session = httpServletRequest.getSession(false);
		if (session != null) {
			session.invalidate();
		}
		SecurityContextHolder.clearContext();
	}

	@Override
	@Transactional(readOnly = true)
	public AuthResponse me(HttpServletRequest httpServletRequest) {
		HttpSession session = httpServletRequest.getSession(false);
		if (session == null) {
			throw new UnauthorizedException("No active session");
		}

		SessionUser sessionUser = (SessionUser) session.getAttribute(SessionConstants.CURRENT_USER);
		if (sessionUser == null) {
			throw new UnauthorizedException("No active session");
		}

		User user = userRepository.findById(sessionUser.id())
			.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		return new AuthResponse("Current session", session.getId(), sessionUser, UserResponse.from(user));
	}
}
