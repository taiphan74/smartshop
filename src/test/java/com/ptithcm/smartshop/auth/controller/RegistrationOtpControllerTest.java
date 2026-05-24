package com.ptithcm.smartshop.auth.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ptithcm.smartshop.auth.command.SendRegistrationOtpCommand;
import com.ptithcm.smartshop.auth.service.RegistrationOtpService;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class RegistrationOtpControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private RegistrationOtpService registrationOtpService;

	@Test
	void returnsGenericOkAndDispatchesCommand() throws Exception {
		ArgumentCaptor<SendRegistrationOtpCommand> captor = ArgumentCaptor.forClass(SendRegistrationOtpCommand.class);

		mockMvc.perform(post("/api/auth/registration/otp")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\" Customer@Example.com \"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message", is("If the email can receive registration OTP, instructions will be sent.")));

		verify(registrationOtpService).sendRegistrationOtp(captor.capture());
		SendRegistrationOtpCommand command = captor.getValue();
		org.assertj.core.api.Assertions.assertThat(command.email()).isEqualTo(" Customer@Example.com ");
		org.assertj.core.api.Assertions.assertThat(command.correlationId()).isNotBlank();
	}

	@Test
	void rejectsInvalidEmailBeforeService() throws Exception {
		mockMvc.perform(post("/api/auth/registration/otp")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"not-email\"}"))
				.andExpect(status().isBadRequest());

		verifyNoInteractions(registrationOtpService);
	}
}
