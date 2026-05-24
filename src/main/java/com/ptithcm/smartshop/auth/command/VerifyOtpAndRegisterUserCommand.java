package com.ptithcm.smartshop.auth.command;

public record VerifyOtpAndRegisterUserCommand(String email, String otp, String password, String fullName, String correlationId) {
}
