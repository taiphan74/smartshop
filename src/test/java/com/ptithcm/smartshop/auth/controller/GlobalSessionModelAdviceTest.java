package com.ptithcm.smartshop.auth.controller;

import com.ptithcm.smartshop.product.dto.CategoryDTO;
import com.ptithcm.smartshop.product.dto.PageResponse;
import com.ptithcm.smartshop.product.service.CategoryService;
import com.ptithcm.smartshop.security.session.SessionConstants;
import com.ptithcm.smartshop.security.session.SessionUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;

import java.util.List;
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
		CategoryService categoryService = mock(CategoryService.class);
		when(categoryService.findParentCategories(0, 50, "name", "asc"))
				.thenReturn(new PageResponse<>(List.of(), 0, 50, 0, 0, true));

		new GlobalSessionModelAdvice(categoryService).addGlobalAttributes(request, model);

		assertThat(model.getAttribute("sessionUser")).isSameAs(sessionUser);
	}

	@Test
	void addsHeaderCategoriesToEveryModel() {
		CategoryDTO books = new CategoryDTO("1", "Sách", "sach", "/sach", 0, null, List.of());
		CategoryDTO phones = new CategoryDTO("2", "Điện thoại", "dien-thoai", "/dien-thoai", 0, null, List.of());
		CategoryService categoryService = mock(CategoryService.class);
		when(categoryService.findParentCategories(0, 50, "name", "asc"))
				.thenReturn(new PageResponse<>(List.of(books, phones), 0, 50, 2, 1, true));
		ConcurrentModel model = new ConcurrentModel();

		new GlobalSessionModelAdvice(categoryService).addGlobalAttributes(mock(HttpServletRequest.class), model);

		assertThat(model.getAttribute("rootCategories")).isEqualTo(List.of(books, phones));
		assertThat(model.getAttribute("headerCategories")).isEqualTo(List.of(books, phones));
	}
}

