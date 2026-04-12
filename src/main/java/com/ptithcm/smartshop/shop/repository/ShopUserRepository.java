package com.ptithcm.smartshop.shop.repository;

import com.ptithcm.smartshop.shop.entity.ShopUser;
import com.ptithcm.smartshop.shop.enums.ShopUserStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopUserRepository extends JpaRepository<ShopUser, UUID> {

	Optional<ShopUser> findByShop_IdAndUser_IdAndStatus(UUID shopId, UUID userId, ShopUserStatus status);
}
