package com.ptithcm.smartshop.auth.repository;

import com.ptithcm.smartshop.auth.entity.RegistrationOtp;
import com.ptithcm.smartshop.auth.enums.RegistrationOtpStatus;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface RegistrationOtpRepository extends JpaRepository<RegistrationOtp, UUID> {

	long countByEmailAndCreatedAtGreaterThanEqual(String email, Instant createdAt);

	List<RegistrationOtp> findByEmailAndStatusOrderByCreatedAtDesc(String email, RegistrationOtpStatus status);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<RegistrationOtp> findFirstByEmailAndStatusAndExpiresAtGreaterThanOrderByCreatedAtDesc(
			String email, RegistrationOtpStatus status, Instant now);
}
