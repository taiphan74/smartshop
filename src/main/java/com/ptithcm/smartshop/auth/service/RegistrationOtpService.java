package com.ptithcm.smartshop.auth.service;

import com.ptithcm.smartshop.auth.command.SendRegistrationOtpCommand;
import com.ptithcm.smartshop.auth.command.VerifyOtpAndRegisterUserCommand;
import com.ptithcm.smartshop.user.dto.UserResponse;

public interface RegistrationOtpService {

	void sendRegistrationOtp(SendRegistrationOtpCommand command);

	UserResponse verifyAndRegister(VerifyOtpAndRegisterUserCommand command);

	UserResponse activateByOtp(String otp, String correlationId);
}
