package com.ptithcm.smartshop.shop.repository;

import com.ptithcm.smartshop.shop.entity.ShopApprovalHistory;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopApprovalHistoryRepository extends JpaRepository<ShopApprovalHistory, UUID> {

	List<ShopApprovalHistory> findByShopIdOrderByCreatedAtDesc(UUID shopId);
}
