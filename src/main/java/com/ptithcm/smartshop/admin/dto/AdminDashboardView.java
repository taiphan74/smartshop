package com.ptithcm.smartshop.admin.dto;

import java.util.List;

public record AdminDashboardView(
		List<AdminMetricCard> metrics,
		List<AdminRecentOrderView> recentOrders,
		List<AdminCategoryProductCountView> categoryProductCounts,
		List<AdminPendingShopView> pendingShops) {
}
