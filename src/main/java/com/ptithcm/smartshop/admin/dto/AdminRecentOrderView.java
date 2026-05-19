package com.ptithcm.smartshop.admin.dto;

import com.ptithcm.smartshop.order.domain.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record AdminRecentOrderView(
		String code,
		String customerName,
		Instant createdAt,
		BigDecimal finalAmount,
		OrderStatus status) {
}
