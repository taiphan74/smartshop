package com.ptithcm.smartshop.admin.service;

import com.ptithcm.smartshop.admin.dto.AdminShopForm;
import com.ptithcm.smartshop.order.domain.repository.OrderRepository;
import com.ptithcm.smartshop.product.repository.ProductRepository;
import com.ptithcm.smartshop.shop.entity.Shop;
import com.ptithcm.smartshop.shop.entity.ShopApprovalHistory;
import com.ptithcm.smartshop.shop.enums.ShopStatus;
import com.ptithcm.smartshop.shop.repository.ShopApprovalHistoryRepository;
import com.ptithcm.smartshop.shop.repository.ShopRepository;
import com.ptithcm.smartshop.user.entity.User;
import com.ptithcm.smartshop.user.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AdminShopApprovalService {

	private final ShopRepository shopRepository;
	private final UserRepository userRepository;
	private final ShopApprovalHistoryRepository historyRepository;
	private final ProductRepository productRepository;
	private final OrderRepository orderRepository;

	public AdminShopApprovalService(
			ShopRepository shopRepository,
			UserRepository userRepository,
			ShopApprovalHistoryRepository historyRepository,
			ProductRepository productRepository,
			OrderRepository orderRepository) {
		this.shopRepository = shopRepository;
		this.userRepository = userRepository;
		this.historyRepository = historyRepository;
		this.productRepository = productRepository;
		this.orderRepository = orderRepository;
	}

	@Transactional(readOnly = true)
	public List<Shop> listShops(ShopStatus status) {
		if (status == null) {
			return shopRepository.findAll();
		}
		return shopRepository.findByStatusOrderByCreatedAtDesc(status);
	}

	@Transactional(readOnly = true)
	public Shop getShop(UUID shopId) {
		return shopRepository.findById(shopId)
				.orElseThrow(() -> new IllegalArgumentException("Shop không tồn tại"));
	}

	@Transactional(readOnly = true)
	public List<ShopApprovalHistory> history(UUID shopId) {
		return historyRepository.findByShopIdOrderByCreatedAtDesc(shopId);
	}

	@Transactional
	public void updateShop(UUID shopId, AdminShopForm form) {
		Shop shop = getShop(shopId);
		if (shopRepository.existsBySlugAndIdNot(form.getSlug(), shopId)) {
			throw new IllegalArgumentException("Slug shop đã tồn tại");
		}
		shop.setName(form.getName().trim());
		shop.setSlug(form.getSlug().trim());
		shop.setLogoUrl(trimToNull(form.getLogoUrl()));
		shop.setBannerUrl(trimToNull(form.getBannerUrl()));
		shop.setEmail(trimToNull(form.getEmail()));
		shop.setPhone(trimToNull(form.getPhone()));
		shop.setDescription(trimToNull(form.getDescription()));
		shop.setStatus(form.getStatus());
	}

	@Transactional
	public void deleteShop(UUID shopId) {
		Shop shop = getShop(shopId);
		if (productRepository.countByShopId(shopId) > 0 || orderRepository.countByShopId(shopId) > 0) {
			throw new IllegalArgumentException("Shop còn dữ liệu liên quan, hãy tạm ngưng thay vì xóa");
		}
		shopRepository.delete(shop);
	}

	@Transactional
	public void approve(UUID shopId, UUID adminUserId) {
		transition(shopId, adminUserId, ShopStatus.APPROVED, "Đã duyệt shop");
	}

	@Transactional
	public void reject(UUID shopId, UUID adminUserId, String reason) {
		requireReason(reason);
		transition(shopId, adminUserId, ShopStatus.REJECTED, reason.trim());
	}

	@Transactional
	public void suspend(UUID shopId, UUID adminUserId, String reason) {
		requireReason(reason);
		transition(shopId, adminUserId, ShopStatus.SUSPENDED, reason.trim());
	}

	private void transition(UUID shopId, UUID adminUserId, ShopStatus newStatus, String reason) {
		Shop shop = shopRepository.findById(shopId)
				.orElseThrow(() -> new IllegalArgumentException("Shop không tồn tại"));
		User adminUser = userRepository.findById(adminUserId)
				.orElseThrow(() -> new IllegalArgumentException("Admin không tồn tại"));
		ShopStatus oldStatus = shop.getStatus();
		if (oldStatus == newStatus) {
			throw new IllegalArgumentException("Shop đã ở trạng thái " + newStatus);
		}

		shop.setStatus(newStatus);
		ShopApprovalHistory history = new ShopApprovalHistory();
		history.setShop(shop);
		history.setAdminUser(adminUser);
		history.setOldStatus(oldStatus);
		history.setNewStatus(newStatus);
		history.setReason(reason);
		historyRepository.save(history);
	}

	private void requireReason(String reason) {
		if (!StringUtils.hasText(reason)) {
			throw new IllegalArgumentException("Vui lòng nhập lý do");
		}
	}

	private String trimToNull(String value) {
		return StringUtils.hasText(value) ? value.trim() : null;
	}
}
