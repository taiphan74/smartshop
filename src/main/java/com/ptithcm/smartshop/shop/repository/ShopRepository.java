package com.ptithcm.smartshop.shop.repository;

import com.ptithcm.smartshop.shop.entity.Shop;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopRepository extends JpaRepository<Shop, UUID> {

	boolean existsBySlug(String slug);

	boolean existsBySlugAndIdNot(String slug, UUID id);

	@EntityGraph(attributePaths = {"owner", "owner.profile", "shopUsers", "shopUsers.user", "shopUsers.user.profile"})
	Optional<Shop> findById(UUID id);

	@EntityGraph(attributePaths = {"owner", "owner.profile", "shopUsers", "shopUsers.user", "shopUsers.user.profile"})
	Optional<Shop> findBySlug(String slug);

	@EntityGraph(attributePaths = {"owner", "owner.profile", "shopUsers", "shopUsers.user", "shopUsers.user.profile"})
	List<Shop> findDistinctByOwner_IdOrShopUsers_User_Id(UUID ownerUserId, UUID userId);
}
