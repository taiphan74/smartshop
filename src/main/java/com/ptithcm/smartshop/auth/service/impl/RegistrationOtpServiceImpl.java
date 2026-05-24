package com.ptithcm.smartshop.auth.service.impl;

import com.ptithcm.smartshop.auth.command.SendRegistrationOtpCommand;
import com.ptithcm.smartshop.auth.entity.RegistrationOtp;
import com.ptithcm.smartshop.auth.enums.RegistrationOtpStatus;
import com.ptithcm.smartshop.auth.repository.RegistrationOtpRepository;
import com.ptithcm.smartshop.auth.service.RegistrationOtpService;
import com.ptithcm.smartshop.user.repository.UserRepository;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationOtpServiceImpl implements RegistrationOtpService {

	private static final Logger log = LoggerFactory.getLogger(RegistrationOtpServiceImpl.class);
	private static final int MAX_REQUESTS = 3;
	private static final long RATE_LIMIT_WINDOW_SECONDS = 300;
	private static final long OTP_TTL_SECONDS = 300;
	private final RegistrationOtpRepository otpRepository;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final SecureRandom secureRandom = new SecureRandom();

	public RegistrationOtpServiceImpl(
			RegistrationOtpRepository otpRepository,
			UserRepository userRepository,
			PasswordEncoder passwordEncoder) {
		this.otpRepository = otpRepository;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
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
			log.info("Registration OTP row created for emailHash={}", Integer.toHexString(normalizedEmail.hashCode()));
		}
	}

	private String normalizeEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}
}
