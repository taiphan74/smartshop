package com.ptithcm.smartshop.auth.controller;

import com.ptithcm.smartshop.auth.command.SendRegistrationOtpCommand;
import com.ptithcm.smartshop.auth.dto.RegistrationOtpResponse;
import com.ptithcm.smartshop.auth.dto.RequestRegistrationOtpDto;
import com.ptithcm.smartshop.auth.service.RegistrationOtpService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/registration")
public class RegistrationOtpController {

	private static final String GENERIC_MESSAGE = "If the email can receive registration OTP, instructions will be sent.";
	private final RegistrationOtpService registrationOtpService;

	public RegistrationOtpController(RegistrationOtpService registrationOtpService) {
		this.registrationOtpService = registrationOtpService;
	}

	@PostMapping("/otp")
	public ResponseEntity<RegistrationOtpResponse> sendRegistrationOtp(@Valid @RequestBody RequestRegistrationOtpDto request) {
		String correlationId = UUID.randomUUID().toString();
		try (MDC.MDCCloseable ignored = MDC.putCloseable("correlationId", correlationId)) {
			registrationOtpService.sendRegistrationOtp(new SendRegistrationOtpCommand(request.email(), correlationId));
			return ResponseEntity.ok(new RegistrationOtpResponse(GENERIC_MESSAGE));
		}
	}
}
