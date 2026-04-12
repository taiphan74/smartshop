package com.ptithcm.smartshop.shop.dto;

import com.ptithcm.smartshop.shop.entity.Shop;
import com.ptithcm.smartshop.shop.enums.ShopStatus;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record ShopResponse(
	UUID id,
	String name,
	String slug,
	String email,
	String phone,
	String logoUrl,
	String bannerUrl,
	String description,
	ShopStatus status,
	UUID ownerUserId,
	String ownerFullName,
	Instant createdAt,
	Instant updatedAt
) {

	public static ShopResponse from(Shop shop) {
		return new ShopResponse(
			shop.getId(),
			shop.getName(),
			shop.getSlug(),
			shop.getEmail(),
			shop.getPhone(),
			shop.getLogoUrl(),
			shop.getBannerUrl(),
			shop.getDescription(),
			shop.getStatus(),
			shop.getOwner() != null ? shop.getOwner().getId() : null,
			shop.getOwner() != null ? Objects.toString(shop.getOwner().getFullName(), null) : null,
			shop.getCreatedAt(),
			shop.getUpdatedAt());
	}
}
