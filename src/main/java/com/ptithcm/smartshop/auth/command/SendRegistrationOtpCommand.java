package com.ptithcm.smartshop.auth.command;

public record SendRegistrationOtpCommand(String email, String correlationId) {
}
