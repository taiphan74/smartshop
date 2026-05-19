package com.ptithcm.smartshop.admin.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AdminPageRenderTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void adminDashboardRendersWithRealServices() throws Exception {
		mockMvc.perform(get("/admin").with(user("admin").roles("ADMIN")))
				.andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Bảng điều khiển tổng quan")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Tổng doanh thu")));
	}

	@Test
	void adminShopApprovalPageRendersWithRealServices() throws Exception {
		mockMvc.perform(get("/admin/shops").with(user("admin").roles("ADMIN")))
				.andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Duyệt shop")));
	}
}
