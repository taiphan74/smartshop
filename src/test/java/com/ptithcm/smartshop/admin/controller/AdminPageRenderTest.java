package com.ptithcm.smartshop.admin.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AdminPageRenderTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void adminDashboardRendersWithAdminDesignSystem() throws Exception {
		mockMvc.perform(get("/admin").with(user("admin").roles("ADMIN")))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("admin-shell")))
				.andExpect(content().string(containsString("admin-sidebar")))
				.andExpect(content().string(containsString("admin-page")))
				.andExpect(content().string(containsString("Bảng điều khiển tổng quan")))
				.andExpect(content().string(containsString("Tổng doanh thu")));
	}

	@Test
	void adminShopApprovalPageRendersWithAdminTableAndToolbar() throws Exception {
		mockMvc.perform(get("/admin/shops").with(user("admin").roles("ADMIN")))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("admin-page-header")))
				.andExpect(content().string(containsString("admin-table")))
				.andExpect(content().string(containsString("Duyệt shop")));
	}

	@Test
	void adminProductsPageRendersWithAdminTable() throws Exception {
		mockMvc.perform(get("/admin/products").with(user("admin").roles("ADMIN")))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("admin-table")))
				.andExpect(content().string(containsString("Quản lý sản phẩm")));
	}
}
