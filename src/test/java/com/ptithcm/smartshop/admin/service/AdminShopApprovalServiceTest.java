package com.ptithcm.smartshop.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ptithcm.smartshop.shop.entity.Shop;
import com.ptithcm.smartshop.shop.entity.ShopApprovalHistory;
import com.ptithcm.smartshop.shop.enums.ShopStatus;
import com.ptithcm.smartshop.shop.repository.ShopApprovalHistoryRepository;
import com.ptithcm.smartshop.shop.repository.ShopRepository;
import com.ptithcm.smartshop.user.entity.User;
import com.ptithcm.smartshop.user.enums.UserStatus;
import com.ptithcm.smartshop.user.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class AdminShopApprovalServiceTest {

	@Autowired
	private AdminShopApprovalService service;

	@Autowired
	private ShopRepository shopRepository;

	@Autowired
	private ShopApprovalHistoryRepository historyRepository;

	@Autowired
	private UserRepository userRepository;

	@Test
	void approveUpdatesShopStatusAndWritesHistory() {
		User admin = user("admin");
		User owner = user("owner");
		Shop shop = shop(owner, ShopStatus.PENDING);

		service.approve(shop.getId(), admin.getId());

		assertThat(shopRepository.findById(shop.getId()).orElseThrow().getStatus()).isEqualTo(ShopStatus.APPROVED);
		List<ShopApprovalHistory> history = historyRepository.findByShopIdOrderByCreatedAtDesc(shop.getId());
		assertThat(history).hasSize(1);
		assertThat(history.get(0).getOldStatus()).isEqualTo(ShopStatus.PENDING);
		assertThat(history.get(0).getNewStatus()).isEqualTo(ShopStatus.APPROVED);
		assertThat(history.get(0).getAdminUser().getId()).isEqualTo(admin.getId());
	}

	@Test
	void rejectRequiresReason() {
		User admin = user("admin-reject");
		User owner = user("owner-reject");
		Shop shop = shop(owner, ShopStatus.PENDING);

		assertThatThrownBy(() -> service.reject(shop.getId(), admin.getId(), " "))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Vui lòng nhập lý do");
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

	private Shop shop(User owner, ShopStatus status) {
		String token = UUID.randomUUID().toString();
		Shop shop = new Shop();
		shop.setOwner(owner);
		shop.setName("Shop " + token);
		shop.setSlug("shop-" + token);
		shop.setEmail("shop-" + token + "@example.com");
		shop.setPhone("0900000000");
		shop.setDescription("Shop test");
		shop.setStatus(status);
		return shopRepository.save(shop);
	}
}
