package com.ptithcm.smartshop.auth.repository;

import com.ptithcm.smartshop.auth.entity.RegistrationOtp;
import com.ptithcm.smartshop.auth.enums.RegistrationOtpStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistrationOtpRepository extends JpaRepository<RegistrationOtp, UUID> {

	long countByEmailAndCreatedAtGreaterThanEqual(String email, Instant createdAt);

	List<RegistrationOtp> findByEmailAndStatusOrderByCreatedAtDesc(String email, RegistrationOtpStatus status);
}
