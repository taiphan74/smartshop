package com.ptithcm.smartshop.admin.service;

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

	public AdminShopApprovalService(
			ShopRepository shopRepository,
			UserRepository userRepository,
			ShopApprovalHistoryRepository historyRepository) {
		this.shopRepository = shopRepository;
		this.userRepository = userRepository;
		this.historyRepository = historyRepository;
	}

	@Transactional(readOnly = true)
	public List<Shop> listShops(ShopStatus status) {
		if (status == null) {
			return shopRepository.findAll();
		}
		return shopRepository.findByStatusOrderByCreatedAtDesc(status);
	}

	@Transactional(readOnly = true)
	public List<ShopApprovalHistory> history(UUID shopId) {
		return historyRepository.findByShopIdOrderByCreatedAtDesc(shopId);
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
}
