package com.ptithcm.smartshop.auth.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.ptithcm.smartshop.auth.entity.RegistrationOtp;
import com.ptithcm.smartshop.auth.enums.RegistrationOtpStatus;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class RegistrationOtpRepositoryTest {

	@Autowired
	private RegistrationOtpRepository repository;

	@Test
	void countsRequestsByEmailSinceCutoff() {
		String email = "new-user@example.com";
		repository.save(otp(email, "hash-1", Instant.now().plusSeconds(300)));
		repository.save(otp(email, "hash-2", Instant.now().plusSeconds(300)));
		repository.save(otp("other@example.com", "hash-3", Instant.now().plusSeconds(300)));

		long count = repository.countByEmailAndCreatedAtGreaterThanEqual(email, Instant.now().minusSeconds(300));

		assertThat(count).isEqualTo(2);
	}

	private RegistrationOtp otp(String email, String hash, Instant expiresAt) {
		RegistrationOtp otp = new RegistrationOtp();
		otp.setEmail(email);
		otp.setOtpHash(hash);
		otp.setStatus(RegistrationOtpStatus.WAIT);
		otp.setExpiresAt(expiresAt);
		otp.setCorrelationId("correlation-test");
		return otp;
	}
}
