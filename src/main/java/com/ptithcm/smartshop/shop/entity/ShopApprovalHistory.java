package com.ptithcm.smartshop.shop.entity;

import com.ptithcm.smartshop.shared.entity.AuditableEntity;
import com.ptithcm.smartshop.shop.enums.ShopStatus;
import com.ptithcm.smartshop.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "shop_approval_history")
public class ShopApprovalHistory extends AuditableEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "shop_id", nullable = false)
	private Shop shop;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "admin_user_id", nullable = false)
	private User adminUser;

	@Enumerated(EnumType.STRING)
	@Column(name = "old_status", nullable = false, length = 20)
	private ShopStatus oldStatus;

	@Enumerated(EnumType.STRING)
	@Column(name = "new_status", nullable = false, length = 20)
	private ShopStatus newStatus;

	@Column(columnDefinition = "text")
	private String reason;

	public Shop getShop() {
		return shop;
	}

	public void setShop(Shop shop) {
		this.shop = shop;
	}

	public User getAdminUser() {
		return adminUser;
	}

	public void setAdminUser(User adminUser) {
		this.adminUser = adminUser;
	}

	public ShopStatus getOldStatus() {
		return oldStatus;
	}

	public void setOldStatus(ShopStatus oldStatus) {
		this.oldStatus = oldStatus;
	}

	public ShopStatus getNewStatus() {
		return newStatus;
	}

	public void setNewStatus(ShopStatus newStatus) {
		this.newStatus = newStatus;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}
}
