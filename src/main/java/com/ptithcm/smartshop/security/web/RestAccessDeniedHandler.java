package com.ptithcm.smartshop.security.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

	private static final Logger log = LoggerFactory.getLogger(RestAccessDeniedHandler.class);

	@Override
	public void handle(
		HttpServletRequest request,
		HttpServletResponse response,
		AccessDeniedException accessDeniedException
	) throws IOException {
		log.warn("Access denied | method={} | path={} | session={}", request.getMethod(), request.getRequestURI(), request.getRequestedSessionId());
		if (!request.getRequestURI().startsWith("/api/")) {
			response.sendRedirect("/?error=forbidden");
			return;
		}
		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.getWriter().write("""
			{"status":403,"error":"Forbidden","message":"You do not have permission to access this resource"}
			""".trim());
	}
}
