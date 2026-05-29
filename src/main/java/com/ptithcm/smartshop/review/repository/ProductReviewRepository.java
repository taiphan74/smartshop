package com.ptithcm.smartshop.review.repository;

import com.ptithcm.smartshop.review.entity.ProductReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReview, UUID> {
    @Override
    @EntityGraph(attributePaths = {"product", "user", "order"})
    Optional<ProductReview> findById(UUID id);

    @Override
    @EntityGraph(attributePaths = {"product", "user", "order"})
    Page<ProductReview> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"product", "user", "order"})
    Page<ProductReview> findByProductId(UUID productId, Pageable pageable);

    @EntityGraph(attributePaths = {"product", "user", "order"})
    Page<ProductReview> findByProductIdAndVisible(UUID productId, Boolean visible, Pageable pageable);

    @EntityGraph(attributePaths = {"product", "user", "order"})
    Page<ProductReview> findByVisible(Boolean visible, Pageable pageable);

    @EntityGraph(attributePaths = {"product", "user", "order"})
    List<ProductReview> findByProductIdAndVisible(UUID productId, Boolean visible);
    boolean existsByProductIdAndUserIdAndOrderId(UUID productId, UUID userId, UUID orderId);
    long countByProductId(UUID productId);
}
