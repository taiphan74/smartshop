package com.ptithcm.smartshop.auth.controller;

import com.ptithcm.smartshop.security.session.SessionConstants;
import com.ptithcm.smartshop.security.session.SessionUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalSessionModelAdviceTest {

	@Test
	void addsSessionUserFromExistingSessionToEveryModel() {
		SessionUser sessionUser = new SessionUser(
				UUID.randomUUID(),
				"customer@example.com",
				"0900000000",
				"Nguyen Van A",
				Set.of("CUSTOMER"));
		HttpSession session = mock(HttpSession.class);
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getSession(false)).thenReturn(session);
		when(session.getAttribute(SessionConstants.CURRENT_USER)).thenReturn(sessionUser);
		ConcurrentModel model = new ConcurrentModel();

		new GlobalSessionModelAdvice().addSessionUser(request, model);

		assertThat(model.getAttribute("sessionUser")).isSameAs(sessionUser);
	}
}
