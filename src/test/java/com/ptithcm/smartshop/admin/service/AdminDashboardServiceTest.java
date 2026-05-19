package com.ptithcm.smartshop.admin.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.ptithcm.smartshop.admin.dto.AdminCategoryProductCountView;
import com.ptithcm.smartshop.admin.dto.AdminDashboardView;
import com.ptithcm.smartshop.admin.dto.AdminMetricCard;
import com.ptithcm.smartshop.admin.dto.AdminPendingShopView;
import com.ptithcm.smartshop.admin.dto.AdminRecentOrderView;
import com.ptithcm.smartshop.order.domain.entity.Order;
import com.ptithcm.smartshop.order.domain.enums.OrderStatus;
import com.ptithcm.smartshop.order.domain.enums.PaymentMethod;
import com.ptithcm.smartshop.order.domain.enums.PaymentStatus;
import com.ptithcm.smartshop.order.domain.repository.OrderRepository;
import com.ptithcm.smartshop.product.entity.Category;
import com.ptithcm.smartshop.product.repository.CategoryRepository;
import com.ptithcm.smartshop.shop.entity.Shop;
import com.ptithcm.smartshop.shop.enums.ShopStatus;
import com.ptithcm.smartshop.shop.repository.ShopRepository;
import com.ptithcm.smartshop.user.entity.User;
import com.ptithcm.smartshop.user.enums.UserStatus;
import com.ptithcm.smartshop.user.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class AdminDashboardServiceTest {

	@Autowired
	private AdminDashboardService service;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ShopRepository shopRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private OrderRepository orderRepository;

	@Test
	void dashboardUsesRealDatabaseRows() {
		User customer = user("customer");
		User owner = user("owner-dashboard");
		Shop approvedShop = shop(owner, ShopStatus.APPROVED, "approved");
		Shop pendingShop = shop(owner, ShopStatus.PENDING, "pending");
		Category category = category();
		shopRepository.flush();
		categoryRepository.flush();
		product(category, approvedShop);
		Order order = order(customer, approvedShop);

		AdminDashboardView dashboard = service.getDashboard();

		assertThat(dashboard.metrics()).extracting(AdminMetricCard::label)
				.contains("Tổng doanh thu", "Đơn hôm nay", "Đơn tháng này", "Khách mới tháng này", "Sản phẩm active", "Shop chờ duyệt");
		assertThat(dashboard.recentOrders()).extracting(AdminRecentOrderView::code).contains(order.getCode());
		assertThat(dashboard.categoryProductCounts()).extracting(AdminCategoryProductCountView::categoryName).contains(category.getName());
		assertThat(dashboard.pendingShops()).extracting(AdminPendingShopView::name).contains(pendingShop.getName());
	}

	private User user(String prefix) {
		String token = UUID.randomUUID().toString();
		User user = new User();
		user.setEmail(prefix + "-" + token + "@example.com");
		user.setPhone(null);
		user.setPasswordHash("{noop}password");
		user.setStatus(UserStatus.ACTIVE);
		user.setFullName(prefix + " user");
		return userRepository.save(user);
	}

	private Shop shop(User owner, ShopStatus status, String prefix) {
		String token = UUID.randomUUID().toString();
		Shop shop = new Shop();
		shop.setOwner(owner);
		shop.setName(prefix + " shop " + token);
		shop.setSlug(prefix + "-shop-" + token);
		shop.setEmail(prefix + "-shop-" + token + "@example.com");
		shop.setPhone("0900000000");
		shop.setDescription("Shop test");
		shop.setStatus(status);
		return shopRepository.save(shop);
	}

	private Category category() {
		String token = UUID.randomUUID().toString();
		Category category = new Category();
		category.setName("Category " + token);
		category.setSlug("category-" + token);
		category.setPath("/category-" + token);
		category.setLevel(0);
		return categoryRepository.save(category);
	}

	private void product(Category category, Shop shop) {
		String token = UUID.randomUUID().toString();
		jdbcTemplate.update("""
				insert into products (
					id, shop_id, category_id, name, slug, description, price, stock_quantity,
					status, created_at, updated_at, review_count, rating_sum, average_rating
				) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
				""",
				UUID.randomUUID(),
				shop.getId(),
				category.getId(),
				"Product " + token,
				"product-" + token,
				"Product test",
				new BigDecimal("100000"),
				10,
				true,
				LocalDateTime.now(),
				LocalDateTime.now(),
				0L,
				0L,
				BigDecimal.ZERO);
	}

	private Order order(User user, Shop shop) {
		String token = UUID.randomUUID().toString();
		Order order = new Order();
		order.setCode("ORD-" + token.substring(0, 8));
		order.setUser(user);
		order.setShop(shop);
		order.setReceiverName("Receiver");
		order.setReceiverPhone("0900000000");
		order.setShippingProvince("HCM");
		order.setShippingDistrict("1");
		order.setShippingWard("Ben Nghe");
		order.setShippingDetail("1 Nguyen Hue");
		order.setTotalAmount(new BigDecimal("100000"));
		order.setShippingFee(BigDecimal.ZERO);
		order.setDiscountAmount(BigDecimal.ZERO);
		order.setFinalAmount(new BigDecimal("100000"));
		order.setStatus(OrderStatus.DELIVERED);
		order.setPaymentMethod(PaymentMethod.COD);
		order.setPaymentStatus(PaymentStatus.PAID);
		return orderRepository.save(order);
	}
}
