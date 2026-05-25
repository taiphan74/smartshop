package com.ptithcm.smartshop.auth.entity;

import com.ptithcm.smartshop.auth.enums.RegistrationOtpStatus;
import com.ptithcm.smartshop.shared.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "registration_otp")
public class RegistrationOtp extends AuditableEntity {

	@Column(nullable = false, length = 150)
	private String email;

	@Column(name = "otp_hash", nullable = false, length = 255)
	private String otpHash;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private RegistrationOtpStatus status = RegistrationOtpStatus.WAIT;

	@Column(name = "expires_at", nullable = false)
	private Instant expiresAt;

	@Column(nullable = false)
	private int attempts;

	@Column(name = "correlation_id", length = 100)
	private String correlationId;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getOtpHash() {
		return otpHash;
	}

	public void setOtpHash(String otpHash) {
		this.otpHash = otpHash;
	}

	public RegistrationOtpStatus getStatus() {
		return status;
	}

	public void setStatus(RegistrationOtpStatus status) {
		this.status = status;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(Instant expiresAt) {
		this.expiresAt = expiresAt;
	}

	public String getCorrelationId() {
		return correlationId;
	}

	public void setCorrelationId(String correlationId) {
		this.correlationId = correlationId;
	}

	public int getAttempts() {
		return attempts;
	}

	public void setAttempts(int attempts) {
		this.attempts = attempts;
	}
}
