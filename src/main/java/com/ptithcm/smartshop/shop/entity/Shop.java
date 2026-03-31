package com.ptithcm.smartshop.shop.entity;

import com.ptithcm.smartshop.common.entity.AuditableEntity;
import com.ptithcm.smartshop.shop.enums.ShopStatus;
import com.ptithcm.smartshop.user.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "shops")
public class Shop extends AuditableEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "owner_user_id", nullable = false)
	private User owner;

	@Column(nullable = false, length = 150)
	private String name;

	@Column(nullable = false, unique = true, length = 160)
	private String slug;

	@Column(name = "logo_url", length = 500)
	private String logoUrl;

	@Column(name = "banner_url", length = 500)
	private String bannerUrl;

	@Column(length = 150)
	private String email;

	@Column(length = 20)
	private String phone;

	@Column(columnDefinition = "text")
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private ShopStatus status = ShopStatus.PENDING;

	@OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<ShopUser> shopUsers = new LinkedHashSet<>();

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSlug() {
		return slug;
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}

	public String getLogoUrl() {
		return logoUrl;
	}

	public void setLogoUrl(String logoUrl) {
		this.logoUrl = logoUrl;
	}

	public String getBannerUrl() {
		return bannerUrl;
	}

	public void setBannerUrl(String bannerUrl) {
		this.bannerUrl = bannerUrl;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public ShopStatus getStatus() {
		return status;
	}

	public void setStatus(ShopStatus status) {
		this.status = status;
	}

	public Set<ShopUser> getShopUsers() {
		return shopUsers;
	}
}
