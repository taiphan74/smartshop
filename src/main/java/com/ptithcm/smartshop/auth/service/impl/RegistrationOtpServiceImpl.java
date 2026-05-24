package com.ptithcm.smartshop.auth.service.impl;

import com.ptithcm.smartshop.auth.command.SendRegistrationOtpCommand;
import com.ptithcm.smartshop.auth.command.VerifyOtpAndRegisterUserCommand;
import com.ptithcm.smartshop.auth.entity.RegistrationOtp;
import com.ptithcm.smartshop.auth.enums.RegistrationOtpStatus;
import com.ptithcm.smartshop.auth.repository.RegistrationOtpRepository;
import com.ptithcm.smartshop.auth.service.RegistrationOtpService;
import com.ptithcm.smartshop.shared.mail.EmailService;
import com.ptithcm.smartshop.security.rbac.entity.Role;
import com.ptithcm.smartshop.security.rbac.repository.RoleRepository;
import com.ptithcm.smartshop.shared.exception.ConflictException;
import com.ptithcm.smartshop.shared.exception.ResourceNotFoundException;
import com.ptithcm.smartshop.user.dto.UserResponse;
import com.ptithcm.smartshop.user.entity.User;
import com.ptithcm.smartshop.user.enums.UserStatus;
import com.ptithcm.smartshop.user.repository.UserRepository;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Locale;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationOtpServiceImpl implements RegistrationOtpService {

	private static final Logger log = LoggerFactory.getLogger(RegistrationOtpServiceImpl.class);
	private static final int MAX_REQUESTS = 3;
	private static final long RATE_LIMIT_WINDOW_SECONDS = 300;
	private static final long OTP_TTL_SECONDS = 300;
	private static final String CUSTOMER_ROLE_CODE = "CUSTOMER";
	private final RegistrationOtpRepository otpRepository;
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	private final EmailService emailService;
	private final SecureRandom secureRandom = new SecureRandom();

	public RegistrationOtpServiceImpl(
			RegistrationOtpRepository otpRepository,
			UserRepository userRepository,
			RoleRepository roleRepository,
			PasswordEncoder passwordEncoder,
			EmailService emailService) {
		this.otpRepository = otpRepository;
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
		this.emailService = emailService;
	}

	@Override
	@Transactional
	public void sendRegistrationOtp(SendRegistrationOtpCommand command) {
		String normalizedEmail = normalizeEmail(command.email());
		try (MDC.MDCCloseable ignored = MDC.putCloseable("correlationId", command.correlationId())) {
			Instant now = Instant.now();
			long recentRequests = otpRepository.countByEmailAndCreatedAtGreaterThanEqual(
					normalizedEmail,
					now.minusSeconds(RATE_LIMIT_WINDOW_SECONDS));
			if (recentRequests >= MAX_REQUESTS) {
				log.info("Registration OTP request rate-limited for emailHash={}", Integer.toHexString(normalizedEmail.hashCode()));
				return;
			}

			if (userRepository.existsByEmail(normalizedEmail)) {
				log.info("Registration OTP request handled for existing account emailHash={}", Integer.toHexString(normalizedEmail.hashCode()));
				return;
			}

			String otp = String.format("%06d", secureRandom.nextInt(1_000_000));
			RegistrationOtp registrationOtp = new RegistrationOtp();
			registrationOtp.setEmail(normalizedEmail);
			registrationOtp.setOtpHash(passwordEncoder.encode(otp));
			registrationOtp.setStatus(RegistrationOtpStatus.WAIT);
			registrationOtp.setExpiresAt(now.plusSeconds(OTP_TTL_SECONDS));
			registrationOtp.setCorrelationId(command.correlationId());
			otpRepository.save(registrationOtp);
			emailService.sendOtp(normalizedEmail, otp);
			log.info("Registration OTP row created for emailHash={}", Integer.toHexString(normalizedEmail.hashCode()));
		}
	}

	@Override
	@Transactional
	public UserResponse verifyAndRegister(VerifyOtpAndRegisterUserCommand command) {
		String normalizedEmail = normalizeEmail(command.email());
		try (MDC.MDCCloseable ignored = MDC.putCloseable("correlationId", command.correlationId())) {
			RegistrationOtp otpRow = otpRepository.findFirstByEmailAndStatusAndExpiresAtGreaterThanOrderByCreatedAtDesc(
					normalizedEmail, RegistrationOtpStatus.WAIT, Instant.now())
					.orElseThrow(() -> new ConflictException("OTP not found or expired"));

			if (!passwordEncoder.matches(command.otp(), otpRow.getOtpHash())) {
				otpRow.setAttempts(otpRow.getAttempts() + 1);
				if (otpRow.getAttempts() >= 3) {
					otpRow.setStatus(RegistrationOtpStatus.EXPIRED);
				}
				otpRepository.save(otpRow);
				throw new ConflictException("OTP is invalid");
			}

			otpRow.setStatus(RegistrationOtpStatus.SUCCESS);
			otpRepository.save(otpRow);

			Role customerRole = roleRepository.findByCode(CUSTOMER_ROLE_CODE)
					.orElseThrow(() -> new ResourceNotFoundException("Default role CUSTOMER was not found"));

			User user = new User();
			user.setEmail(normalizedEmail);
			user.setPassword(passwordEncoder.encode(command.password()));
			user.setFullName(command.fullName());
			user.setStatus(UserStatus.ACTIVE);
			user.setRoles(Set.of(customerRole));

			User savedUser;
			try {
				savedUser = userRepository.save(user);
			} catch (DataIntegrityViolationException e) {
				throw new ConflictException("Email already exists");
			}

			log.info("User registered via OTP for emailHash={}", Integer.toHexString(normalizedEmail.hashCode()));
			return UserResponse.from(savedUser);
		}
	}

	private String normalizeEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}
}
