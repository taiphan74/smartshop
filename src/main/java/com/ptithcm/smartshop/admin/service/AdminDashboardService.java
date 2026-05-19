package com.ptithcm.smartshop.admin.service;

import com.ptithcm.smartshop.admin.dto.AdminCategoryProductCountView;
import com.ptithcm.smartshop.admin.dto.AdminDashboardView;
import com.ptithcm.smartshop.admin.dto.AdminMetricCard;
import com.ptithcm.smartshop.admin.dto.AdminPendingShopView;
import com.ptithcm.smartshop.admin.dto.AdminRecentOrderView;
import com.ptithcm.smartshop.order.domain.entity.Order;
import com.ptithcm.smartshop.order.domain.enums.OrderStatus;
import com.ptithcm.smartshop.order.domain.repository.OrderRepository;
import com.ptithcm.smartshop.product.repository.ProductRepository;
import com.ptithcm.smartshop.shop.entity.Shop;
import com.ptithcm.smartshop.shop.enums.ShopStatus;
import com.ptithcm.smartshop.shop.repository.ShopRepository;
import com.ptithcm.smartshop.user.entity.User;
import com.ptithcm.smartshop.user.repository.UserRepository;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminDashboardService {

	private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
	private static final List<OrderStatus> REVENUE_STATUSES = List.of(OrderStatus.DELIVERED);

	private final OrderRepository orderRepository;
	private final ProductRepository productRepository;
	private final UserRepository userRepository;
	private final ShopRepository shopRepository;

	public AdminDashboardService(
			OrderRepository orderRepository,
			ProductRepository productRepository,
			UserRepository userRepository,
			ShopRepository shopRepository) {
		this.orderRepository = orderRepository;
		this.productRepository = productRepository;
		this.userRepository = userRepository;
		this.shopRepository = shopRepository;
	}

	public AdminDashboardView getDashboard() {
		LocalDate today = LocalDate.now(BUSINESS_ZONE);
		Instant startOfToday = today.atStartOfDay(BUSINESS_ZONE).toInstant();
		Instant startOfTomorrow = today.plusDays(1).atStartOfDay(BUSINESS_ZONE).toInstant();
		Instant startOfMonth = today.with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay(BUSINESS_ZONE).toInstant();
		Instant startOfNextMonth = today.with(TemporalAdjusters.firstDayOfNextMonth()).atStartOfDay(BUSINESS_ZONE).toInstant();

		BigDecimal revenue = orderRepository.sumFinalAmountByStatusIn(REVENUE_STATUSES);
		long ordersToday = orderRepository.countByCreatedAtBetween(startOfToday, startOfTomorrow);
		long ordersThisMonth = orderRepository.countByCreatedAtBetween(startOfMonth, startOfNextMonth);
		long newCustomersThisMonth = userRepository.countByCreatedAtBetween(startOfMonth, startOfNextMonth);
		long activeProducts = productRepository.countByStatusTrue();
		long pendingShops = shopRepository.countByStatus(ShopStatus.PENDING);

		List<AdminMetricCard> metrics = List.of(
				new AdminMetricCard("Tổng doanh thu", formatCurrency(revenue), "Đơn đã giao", "emerald"),
				new AdminMetricCard("Đơn hôm nay", formatNumber(ordersToday), "Tất cả trạng thái", "blue"),
				new AdminMetricCard("Đơn tháng này", formatNumber(ordersThisMonth), "Tất cả trạng thái", "indigo"),
				new AdminMetricCard("Khách mới tháng này", formatNumber(newCustomersThisMonth), "Tài khoản mới", "rose"),
				new AdminMetricCard("Sản phẩm active", formatNumber(activeProducts), "Đang hiển thị", "amber"),
				new AdminMetricCard("Shop chờ duyệt", formatNumber(pendingShops), "Cần admin xử lý", "purple"));

		return new AdminDashboardView(
				metrics,
				recentOrders(),
				categoryProductCounts(),
				pendingShopPreview());
	}

	private List<AdminRecentOrderView> recentOrders() {
		return orderRepository.findByOrderByCreatedAtDesc(PageRequest.of(0, 8)).stream()
				.map(this::toRecentOrderView)
				.toList();
	}

	private AdminRecentOrderView toRecentOrderView(Order order) {
		return new AdminRecentOrderView(
				order.getCode(),
				displayName(order.getUser()),
				order.getCreatedAt(),
				order.getFinalAmount(),
				order.getStatus());
	}

	private List<AdminCategoryProductCountView> categoryProductCounts() {
		return productRepository.countActiveProductsByCategory().stream()
				.map(row -> new AdminCategoryProductCountView(row.getCategoryName(), row.getProductCount()))
				.toList();
	}

	private List<AdminPendingShopView> pendingShopPreview() {
		return shopRepository.findByStatusOrderByCreatedAtDesc(ShopStatus.PENDING, PageRequest.of(0, 5)).stream()
				.map(this::toPendingShopView)
				.toList();
	}

	private AdminPendingShopView toPendingShopView(Shop shop) {
		return new AdminPendingShopView(
				shop.getId(),
				shop.getName(),
				displayName(shop.getOwner()),
				shop.getCreatedAt());
	}

	private String displayName(User user) {
		String fullName = user.getFullName();
		return fullName == null || fullName.isBlank() ? user.getEmail() : fullName;
	}

	private String formatCurrency(BigDecimal amount) {
		NumberFormat format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"));
		return format.format(amount == null ? BigDecimal.ZERO : amount);
	}

	private String formatNumber(long value) {
		return NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN")).format(value);
	}
}
