package com.ptithcm.smartshop.auth.service;

import com.ptithcm.smartshop.auth.command.SendRegistrationOtpCommand;

public interface RegistrationOtpService {

	void sendRegistrationOtp(SendRegistrationOtpCommand command);
}
