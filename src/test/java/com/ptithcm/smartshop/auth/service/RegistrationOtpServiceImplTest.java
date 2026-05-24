package com.ptithcm.smartshop.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ptithcm.smartshop.auth.command.SendRegistrationOtpCommand;
import com.ptithcm.smartshop.auth.entity.RegistrationOtp;
import com.ptithcm.smartshop.auth.enums.RegistrationOtpStatus;
import com.ptithcm.smartshop.auth.repository.RegistrationOtpRepository;
import com.ptithcm.smartshop.auth.service.impl.RegistrationOtpServiceImpl;
import com.ptithcm.smartshop.user.repository.UserRepository;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class RegistrationOtpServiceImplTest {

	private final RegistrationOtpRepository otpRepository = mock(RegistrationOtpRepository.class);
	private final UserRepository userRepository = mock(UserRepository.class);
	private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
	private final RegistrationOtpService service = new RegistrationOtpServiceImpl(
			otpRepository,
			userRepository,
			passwordEncoder);

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
}
