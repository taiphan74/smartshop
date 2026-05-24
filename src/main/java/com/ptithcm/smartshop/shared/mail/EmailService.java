package com.ptithcm.smartshop.shared.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

	private static final Logger log = LoggerFactory.getLogger(EmailService.class);
	private final JavaMailSender mailSender;

	public EmailService(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	public void send(String to, String subject, String body) {
		String correlationId = MDC.get("correlationId");
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setTo(to);
			message.setSubject(subject);
			message.setText(body);
			log.info("Sending email | to={} | subject={} | correlationId={}", maskEmail(to), subject, correlationId);
			mailSender.send(message);
			log.info("Email sent successfully | to={} | subject={} | correlationId={}", maskEmail(to), subject, correlationId);
		} catch (MailException e) {
			log.error("Failed to send email | to={} | subject={} | correlationId={} | error={}", maskEmail(to), subject, correlationId, e.getMessage());
			log.debug("Email send stacktrace", e);
		}
	}

	public void sendOtp(String to, String otp) {
		String verifyLink = "http://localhost:8080/auth/verify?otp=" + otp;
		String body = """
				Your SmartShop registration OTP: %s

				Or click the link below to verify your email:
				%s

				This OTP expires in 5 minutes.
				""".formatted(otp, verifyLink);
		send(to, "Xác thực tài khoản SmartShop", body);
	}

	private String maskEmail(String email) {
		int at = email.indexOf('@');
		if (at <= 1) return email;
		return email.charAt(0) + "***" + email.substring(at);
	}
}
