package com.ptithcm.smartshop.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ptithcm.smartshop.auth.command.SendRegistrationOtpCommand;
import com.ptithcm.smartshop.auth.command.VerifyOtpAndRegisterUserCommand;
import com.ptithcm.smartshop.auth.entity.RegistrationOtp;
import com.ptithcm.smartshop.auth.enums.RegistrationOtpStatus;
import com.ptithcm.smartshop.auth.repository.RegistrationOtpRepository;
import com.ptithcm.smartshop.auth.service.impl.RegistrationOtpServiceImpl;
import com.ptithcm.smartshop.security.rbac.entity.Role;
import com.ptithcm.smartshop.security.rbac.repository.RoleRepository;
import com.ptithcm.smartshop.shared.exception.ConflictException;
import com.ptithcm.smartshop.shared.mail.EmailService;
import com.ptithcm.smartshop.user.dto.UserResponse;
import com.ptithcm.smartshop.user.repository.UserRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class RegistrationOtpServiceImplTest {

	private final RegistrationOtpRepository otpRepository = mock(RegistrationOtpRepository.class);
	private final UserRepository userRepository = mock(UserRepository.class);
	private final RoleRepository roleRepository = mock(RoleRepository.class);
	private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
	private final EmailService emailService = mock(EmailService.class);
	private final RegistrationOtpService service = new RegistrationOtpServiceImpl(
			otpRepository,
			userRepository,
			roleRepository,
			passwordEncoder,
			emailService);

	@BeforeEach
	void setUp() {
		Role customerRole = new Role();
		customerRole.setCode("CUSTOMER");
		when(roleRepository.findByCode("CUSTOMER")).thenReturn(Optional.of(customerRole));
	}

	@Test
	void storesHashedOtpForNewUser() {
		when(otpRepository.countByEmailAndCreatedAtGreaterThanEqual(eq("new@example.com"), any(Instant.class))).thenReturn(0L);
		when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
		ArgumentCaptor<RegistrationOtp> captor = ArgumentCaptor.forClass(RegistrationOtp.class);

		service.sendRegistrationOtp(new SendRegistrationOtpCommand(" New@Example.com ", "corr-1"));

		verify(otpRepository).save(captor.capture());
		RegistrationOtp saved = captor.getValue();
		assertThat(saved.getEmail()).isEqualTo("new@example.com");
		assertThat(saved.getStatus()).isEqualTo(RegistrationOtpStatus.WAIT);
		assertThat(saved.getCorrelationId()).isEqualTo("corr-1");
		assertThat(saved.getExpiresAt()).isAfter(Instant.now().plusSeconds(250));
		assertThat(saved.getOtpHash()).isNotBlank();
		assertThat(saved.getOtpHash()).doesNotMatch("\\d{6}");
	}

	@Test
	void doesNotStoreOtpWhenEmailAlreadyExists() {
		when(otpRepository.countByEmailAndCreatedAtGreaterThanEqual(eq("exists@example.com"), any(Instant.class))).thenReturn(0L);
		when(userRepository.existsByEmail("exists@example.com")).thenReturn(true);

		service.sendRegistrationOtp(new SendRegistrationOtpCommand("exists@example.com", "corr-2"));

		verify(otpRepository, never()).save(any());
	}

	@Test
	void doesNotStoreOtpWhenRateLimitExceeded() {
		when(otpRepository.countByEmailAndCreatedAtGreaterThanEqual(eq("busy@example.com"), any(Instant.class))).thenReturn(3L);

		service.sendRegistrationOtp(new SendRegistrationOtpCommand("busy@example.com", "corr-3"));

		verify(userRepository, never()).existsByEmail(any());
		verify(otpRepository, never()).save(any());
	}

	@Test
	void registersUserWithValidOtp() {
		String email = "register@example.com";
		String otpPlain = "123456";
		String otpHash = passwordEncoder.encode(otpPlain);
		RegistrationOtp validOtp = otpEntity(email, otpHash, RegistrationOtpStatus.WAIT, Instant.now().plusSeconds(300), 0);

		when(otpRepository.findFirstByEmailAndStatusAndExpiresAtGreaterThanOrderByCreatedAtDesc(eq(email), eq(RegistrationOtpStatus.WAIT), any(Instant.class))).thenReturn(Optional.of(validOtp));
		when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

		UserResponse response = service.verifyAndRegister(
				new VerifyOtpAndRegisterUserCommand(email, otpPlain, "password123", "Test User", "corr-verify"));

		assertThat(response).isNotNull();
		assertThat(response.email()).isEqualTo(email);
		verify(otpRepository).save(validOtp);
		assertThat(validOtp.getStatus()).isEqualTo(RegistrationOtpStatus.SUCCESS);
	}

	@Test
	void throwsWhenOtpNotFoundOrExpired() {
		when(otpRepository.findFirstByEmailAndStatusAndExpiresAtGreaterThanOrderByCreatedAtDesc(eq("missing@example.com"), eq(RegistrationOtpStatus.WAIT), any(Instant.class))).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.verifyAndRegister(
				new VerifyOtpAndRegisterUserCommand("missing@example.com", "000000", "password123", "Test", "corr-4")))
				.isInstanceOf(ConflictException.class)
				.hasMessage("OTP not found or expired");
	}

	@Test
	void throwsWhenOtpInvalidAndExpiresAfterMaxAttempts() {
		String email = "bad-otp@example.com";
		String wrongOtp = "000001";
		RegistrationOtp otpRow = otpEntity(email, passwordEncoder.encode("999999"), RegistrationOtpStatus.WAIT, Instant.now().plusSeconds(300), 2);

		when(otpRepository.findFirstByEmailAndStatusAndExpiresAtGreaterThanOrderByCreatedAtDesc(eq(email), eq(RegistrationOtpStatus.WAIT), any(Instant.class))).thenReturn(Optional.of(otpRow));

		assertThatThrownBy(() -> service.verifyAndRegister(
				new VerifyOtpAndRegisterUserCommand(email, wrongOtp, "password123", "Test", "corr-5")))
				.isInstanceOf(ConflictException.class)
				.hasMessage("OTP is invalid");

		assertThat(otpRow.getAttempts()).isEqualTo(3);
		assertThat(otpRow.getStatus()).isEqualTo(RegistrationOtpStatus.EXPIRED);
	}

	private RegistrationOtp otpEntity(String email, String otpHash, RegistrationOtpStatus status, Instant expiresAt, int attempts) {
		RegistrationOtp otp = new RegistrationOtp();
		otp.setEmail(email);
		otp.setOtpHash(otpHash);
		otp.setStatus(status);
		otp.setExpiresAt(expiresAt);
		otp.setCorrelationId("test-corr");
		otp.setAttempts(attempts);
		return otp;
	}
}
