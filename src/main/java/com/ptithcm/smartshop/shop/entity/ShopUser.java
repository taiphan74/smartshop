package com.ptithcm.smartshop.shop.entity;

import com.ptithcm.smartshop.shared.entity.AuditableEntity;
import com.ptithcm.smartshop.shop.enums.ShopUserRole;
import com.ptithcm.smartshop.shop.enums.ShopUserStatus;
import com.ptithcm.smartshop.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(
	name = "shop_users",
	uniqueConstraints = @UniqueConstraint(name = "uk_shop_user", columnNames = {"shop_id", "user_id"})
)
public class ShopUser extends AuditableEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "shop_id", nullable = false)
	private Shop shop;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(name = "shop_role", nullable = false, length = 20)
	private ShopUserRole shopRole = ShopUserRole.STAFF;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private ShopUserStatus status = ShopUserStatus.ACTIVE;

	@CreationTimestamp
	@Column(name = "joined_at", nullable = false, updatable = false)
	private Instant joinedAt;

	public Shop getShop() {
		return shop;
	}

	public void setShop(Shop shop) {
		this.shop = shop;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public ShopUserRole getShopRole() {
		return shopRole;
	}

	public void setShopRole(ShopUserRole shopRole) {
		this.shopRole = shopRole;
	}

	public ShopUserStatus getStatus() {
		return status;
	}

	public void setStatus(ShopUserStatus status) {
		this.status = status;
	}

	public Instant getJoinedAt() {
		return joinedAt;
	}
}

