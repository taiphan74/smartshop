package com.ptithcm.smartshop.auth.service.impl;

import com.ptithcm.smartshop.auth.dto.AuthLoginRequest;
import com.ptithcm.smartshop.auth.dto.AuthRegisterRequest;
import com.ptithcm.smartshop.auth.dto.AuthResponse;
import com.ptithcm.smartshop.auth.entity.RegistrationOtp;
import com.ptithcm.smartshop.auth.enums.RegistrationOtpStatus;
import com.ptithcm.smartshop.auth.repository.RegistrationOtpRepository;
import com.ptithcm.smartshop.auth.service.AuthService;
import com.ptithcm.smartshop.security.rbac.entity.Role;
import com.ptithcm.smartshop.security.rbac.repository.RoleRepository;
import com.ptithcm.smartshop.shared.exception.ConflictException;
import com.ptithcm.smartshop.shared.exception.ResourceNotFoundException;
import com.ptithcm.smartshop.shared.mail.EmailService;
import com.ptithcm.smartshop.user.dto.UserResponse;
import com.ptithcm.smartshop.user.entity.User;
import com.ptithcm.smartshop.user.enums.UserStatus;
import com.ptithcm.smartshop.user.repository.UserRepository;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Locale;
import java.util.Set;
import com.ptithcm.smartshop.security.principal.CustomUserDetails;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AuthServiceImpl implements AuthService {

    private static final String CUSTOMER_ROLE_CODE = "CUSTOMER";
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final RegistrationOtpRepository otpRepository;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            RegistrationOtpRepository otpRepository,
            EmailService emailService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.otpRepository = otpRepository;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public AuthResponse register(AuthRegisterRequest request) {
        String normalizedPhone = normalizePhone(request.phone());

        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already exists");
        }
        if (StringUtils.hasText(normalizedPhone) && userRepository.existsByPhone(normalizedPhone)) {
            throw new ConflictException("Phone already exists");
        }

        Role customerRole = roleRepository.findByCode(CUSTOMER_ROLE_CODE)
                .orElseThrow(() -> new ResourceNotFoundException("Default role CUSTOMER was not found"));

        User user = new User();
        user.setEmail(request.email());
        user.setPhone(normalizedPhone);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());
        user.setStatus(UserStatus.PENDING);
        user.setRoles(Set.of(customerRole));

        User savedUser = userRepository.save(user);

        String otp = String.format("%06d", secureRandom.nextInt(1_000_000));
        RegistrationOtp regOtp = new RegistrationOtp();
        regOtp.setEmail(request.email().trim().toLowerCase(Locale.ROOT));
        regOtp.setOtpHash(passwordEncoder.encode(otp));
        regOtp.setStatus(RegistrationOtpStatus.WAIT);
        regOtp.setExpiresAt(Instant.now().plusSeconds(300));
        otpRepository.save(regOtp);
        emailService.sendOtp(request.email(), otp);

        return new AuthResponse("Register successful", null, null, UserResponse.from(savedUser));
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(AuthLoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.emailOrPhone(), request.password()));

        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return new AuthResponse("Login successful", null, null, UserResponse.from(user));
    }

    @Override
    @Transactional
    public UserResponse activateUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        return UserResponse.from(user);
    }

    private String normalizePhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            return null;
        }
        return phone.trim();
    }
}
