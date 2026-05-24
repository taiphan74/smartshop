package com.ptithcm.smartshop.shared.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setTo(to);
			message.setSubject(subject);
			message.setText(body);
			mailSender.send(message);
			log.info("Email sent to {}", maskEmail(to));
		} catch (MailException e) {
			log.error("Failed to send email to {}", maskEmail(to), e);
		}
	}

	public void sendOtp(String to, String otp) {
		send(to, "Your SmartShop registration OTP", "Your OTP: " + otp + "\n\nThis OTP expires in 5 minutes.");
	}

	private String maskEmail(String email) {
		int at = email.indexOf('@');
		if (at <= 1) return email;
		return email.charAt(0) + "***" + email.substring(at);
	}
}
