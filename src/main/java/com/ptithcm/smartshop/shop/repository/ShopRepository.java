package com.ptithcm.smartshop.shop.repository;

import com.ptithcm.smartshop.shop.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ShopRepository extends JpaRepository<Shop, UUID> {
    boolean existsBySlug(String slug);

    List<Shop> findByOwnerIdOrderByCreatedAtDesc(UUID ownerId);
}
