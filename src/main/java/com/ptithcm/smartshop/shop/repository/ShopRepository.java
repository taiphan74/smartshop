package com.ptithcm.smartshop.shop.repository;

import com.ptithcm.smartshop.shop.entity.Shop;
import com.ptithcm.smartshop.shop.enums.ShopStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShopRepository extends JpaRepository<Shop, UUID> {
    @Override
    @EntityGraph(attributePaths = {"owner", "owner.profile"})
    Optional<Shop> findById(UUID id);

    @Override
    @EntityGraph(attributePaths = {"owner", "owner.profile"})
    List<Shop> findAll();

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, UUID id);

    List<Shop> findByOwnerIdOrderByCreatedAtDesc(UUID ownerId);

    long countByStatus(ShopStatus status);

    @EntityGraph(attributePaths = {"owner", "owner.profile"})
    List<Shop> findByStatusOrderByCreatedAtDesc(ShopStatus status);

    @EntityGraph(attributePaths = {"owner", "owner.profile"})
    List<Shop> findByStatusOrderByCreatedAtDesc(ShopStatus status, Pageable pageable);
}
