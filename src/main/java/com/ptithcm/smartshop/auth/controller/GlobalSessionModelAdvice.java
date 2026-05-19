package com.ptithcm.smartshop.auth.controller;

import com.ptithcm.smartshop.product.dto.CategoryDTO;
import com.ptithcm.smartshop.product.dto.PageResponse;
import com.ptithcm.smartshop.product.service.CategoryService;
import com.ptithcm.smartshop.security.session.SessionConstants;
import com.ptithcm.smartshop.security.session.SessionUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
public class GlobalSessionModelAdvice {

	private final CategoryService categoryService;

	public GlobalSessionModelAdvice(CategoryService categoryService) {
		this.categoryService = categoryService;
	}

	@ModelAttribute
	public void addGlobalAttributes(HttpServletRequest request, Model model) {
		addSessionUser(request, model);
		addHeaderCategories(model);
	}

	private void addSessionUser(HttpServletRequest request, Model model) {
		HttpSession session = request.getSession(false);
		if (session == null) {
			return;
		}

		SessionUser sessionUser = (SessionUser) session.getAttribute(SessionConstants.CURRENT_USER);
		if (sessionUser != null) {
			model.addAttribute("sessionUser", sessionUser);
		}
	}

	private void addHeaderCategories(Model model) {
		PageResponse<CategoryDTO> categoryPage = categoryService.findParentCategories(0, 50, "name", "asc");
		List<CategoryDTO> rootCategories = categoryPage.getContent();
		model.addAttribute("rootCategories", rootCategories);
		model.addAttribute("headerCategories", rootCategories.stream().limit(8).toList());
	}
}
