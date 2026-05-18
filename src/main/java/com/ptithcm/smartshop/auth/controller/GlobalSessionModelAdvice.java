package com.ptithcm.smartshop.auth.controller;

import com.ptithcm.smartshop.security.session.SessionConstants;
import com.ptithcm.smartshop.security.session.SessionUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalSessionModelAdvice {

	@ModelAttribute
	public void addSessionUser(HttpServletRequest request, Model model) {
		HttpSession session = request.getSession(false);
		if (session == null) {
			return;
		}

		SessionUser sessionUser = (SessionUser) session.getAttribute(SessionConstants.CURRENT_USER);
		if (sessionUser != null) {
			model.addAttribute("sessionUser", sessionUser);
		}
	}
}
